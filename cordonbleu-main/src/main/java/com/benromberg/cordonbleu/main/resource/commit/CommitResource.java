package com.benromberg.cordonbleu.main.resource.commit;

import static java.util.stream.Collectors.toList;
import com.benromberg.cordonbleu.data.model.CodeRepositoryMetadata;
import com.benromberg.cordonbleu.data.model.Commit;
import com.benromberg.cordonbleu.data.model.CommitFilePath;
import com.benromberg.cordonbleu.service.coderepository.CodeRepositoryService;
import com.benromberg.cordonbleu.service.commit.CommitHighlightService;
import com.benromberg.cordonbleu.service.commit.CommitService;
import com.benromberg.cordonbleu.service.commit.HighlightedCommit;
import com.benromberg.cordonbleu.service.commit.HighlightedCommitFile;
import com.benromberg.cordonbleu.service.diff.DiffFragment;
import com.benromberg.cordonbleu.service.diff.RelevantCodeLine;
import com.benromberg.cordonbleu.service.diff.RelevantDiffService;
import com.benromberg.cordonbleu.service.diff.SpacerCodeLine;
import io.dropwizard.auth.Auth;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.main.permission.CommitPermissionGuard;
import com.benromberg.cordonbleu.main.permission.UserWithPermissions;
import com.benromberg.cordonbleu.main.resource.comment.CommentEnhancer;
import com.codahale.metrics.annotation.Timed;

@Path("/commit")
@Produces(MediaType.APPLICATION_JSON)
public class CommitResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitResource.class);

    private final CodeRepositoryService codeRepositoryService;
    private final RelevantDiffService relevantDiffService;
    private final CommitService commitService;
    private final CommentEnhancer commentEnhancer;
    private final CommitHighlightService commitHighlightService;
    private final CommitPermissionGuard commitPermissionGuard;

    @Inject
    public CommitResource(CodeRepositoryService codeRepositoryService, RelevantDiffService relevantDiffService,
            CommitService commitService, CommentEnhancer commentEnhancer,
            CommitHighlightService commitHighlightService, CommitPermissionGuard commitPermissionGuard) {
        this.codeRepositoryService = codeRepositoryService;
        this.relevantDiffService = relevantDiffService;
        this.commitService = commitService;
        this.commentEnhancer = commentEnhancer;
        this.commitHighlightService = commitHighlightService;
        this.commitPermissionGuard = commitPermissionGuard;
    }

    @POST
    @Path("/list")
    @Timed
    public List<CommitListItemResponse> getCommits(@Auth(required = false) UserWithPermissions user,
            CommitListRequest request) {
        List<CodeRepositoryMetadata> repositories = commitPermissionGuard.guardListCommits(user,
                request.getRepositories());
        List<Commit> commits = codeRepositoryService.getCommitsForFilter(request.toFilter(), repositories);
        return commits.stream().map(commit -> new CommitListItemResponse(commit)).collect(toList());
    }

    @GET
    @Path("/detail")
    @Timed
    public CommitDetailResponse getCommitDetail(@Auth(required = false) UserWithPermissions user,
            @QueryParam("hash") String hash, @QueryParam("teamId") String teamId) {
        Commit commit = commitPermissionGuard.guardCommitDetail(user, hash, teamId);
        if (commit.isRemoved()) {
            throw new ClientErrorException(Response.Status.GONE);
        }
        long startTime = System.currentTimeMillis();
        HighlightedCommit highlightedCommit = commitHighlightService.highlight(commit);
        List<CommitFileResponse> fileResponses = highlightedCommit
                .getFiles()
                .stream()
                .map(file -> {
                    List<RelevantCodeLine> codeLines = relevantDiffService.diffCodeLines(
                            highlightedCommit.getComments(), file);
                    List<DiffFragment> pathDiff = relevantDiffService.diffPaths(file.getPath());
                    return new CommitFileResponse(file, pathDiff, linesToResponse(highlightedCommit, file, codeLines));
                }).collect(toList());
        CommitDetailResponse response = new CommitDetailResponse(highlightedCommit, fileResponses);
        LOGGER.info("getCommitDetail for {} took {} ms.", hash, System.currentTimeMillis() - startTime);
        return response;
    }

    private List<LineResponse> linesToResponse(HighlightedCommit commit, HighlightedCommitFile file,
            List<RelevantCodeLine> codeLines) {
        return codeLines.stream()
                .map(codeLine -> new LineResponse(commentEnhancer.convertComments(commit), file, codeLine))
                .collect(toList());
    }

    @GET
    @Path("/detail/spacerLines")
    @Timed
    public List<LineResponse> getSpacerLines(@Auth(required = false) UserWithPermissions user,
            @QueryParam("hash") String hash, @QueryParam("teamId") String teamId,
            @QueryParam("beforePath") Optional<String> beforePath, @QueryParam("afterPath") Optional<String> afterPath,
            @QueryParam("beginIndex") Optional<Integer> beginIndex, @QueryParam("endIndex") Optional<Integer> endIndex) {
        Commit commit = commitPermissionGuard.guardCommitDetail(user, hash, teamId);
        HighlightedCommit highlightedCommit = commitHighlightService.highlight(commit);
        CommitFilePath commitFilePath = new CommitFilePath(beforePath, afterPath);
        HighlightedCommitFile commitFile = highlightedCommit.getFiles().stream()
                .filter(file -> file.getPath().equals(commitFilePath)).findFirst().get();
        List<RelevantCodeLine> spacerLines = relevantDiffService.expandSpacer(commitFile, new SpacerCodeLine(
                beginIndex, endIndex));
        return linesToResponse(highlightedCommit, commitFile, spacerLines);
    }

    @POST
    @Path("/approve")
    @Timed
    public CommitApprovalResponse approveCommit(ApprovalRequest request, @Auth UserWithPermissions user) {
        Commit commit = commitPermissionGuard.guardApproval(user, request.getCommitId());
        return new CommitApprovalResponse(commitService.approve(commit.getId(), user.getUser()).get());
    }

    @POST
    @Path("/revertApproval")
    @Timed
    public void revertApprovalOnCommit(ApprovalRequest request, @Auth UserWithPermissions user) {
        Commit commit = commitPermissionGuard.guardApproval(user, request.getCommitId());
        if (!commitService.revertApproval(commit.getId())) {
            throw new NotFoundException();
        }
    }

    @POST
    @Path("/proposeToCollectiveReview")
    @Timed
    public void proposeToCollectiveReview(ProposeToCodeReviewRequest request, @Auth UserWithPermissions user) {
    	Commit commit = commitPermissionGuard.guardApproval(user, request.getCommitId());
    	
    	commitService.proposeToCollectiveReview(commit.getId(), request.getValue());
    }
    
    @GET
    @Path("/notifications")
    @Timed
    public CommitNotificationsResponse getNotifications(@Auth UserWithPermissions user, @QueryParam("limit") int limit) {
        return new CommitNotificationsResponse(commitService.findNotifications(user.getUser(), limit));
    }
}
