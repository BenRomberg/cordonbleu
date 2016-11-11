package com.benromberg.cordonbleu.service.highlight;

import static org.assertj.core.api.Assertions.assertThat;
import com.benromberg.cordonbleu.data.dao.DaoRule;
import com.benromberg.cordonbleu.data.dao.UserDao;
import com.benromberg.cordonbleu.data.model.User;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.benromberg.cordonbleu.service.highlight.TextHighlightResult;
import com.benromberg.cordonbleu.service.highlight.TextHighlightService;

public class TextHighlightServiceTest {
    private static final String OTHER_NAME = "other-name";
    private static final String USER_NAME = "user-name";
    private static final String COMMIT_HASH = "29e927a1ae3cd89cb957c8d0408e5506d6760fc9";
    private static final String COMMIT_PATH_PREFIX = "http://localhost/";
    private static final String HTML_TEXT_ESCAPED = "&lt;script&gt;text&lt;/script&gt;";
    private static final String HTML_TEXT = "<script>text</script>";
    private static final String MARKDOWN_TEXT = "*emphasized*";

    @Rule
    public DaoRule databaseRule = new DaoRule();

    private final UserDao userDao = databaseRule.createUserDao();
    private TextHighlightService service = new TextHighlightService(() -> Long.MAX_VALUE, userDao);

    @Test
    public void markdown_UsesMarkdown() throws Exception {
        String highlightedComment = markdownToHtml(MARKDOWN_TEXT);
        assertThat(highlightedComment).isEqualTo("<p><em>emphasized</em></p>");
    }

    @Test
    public void markdown_OnTimeout_SkipsHighlighting() throws Exception {
        setupServiceTimingOut();
        String highlightedComment = markdownToHtml(MARKDOWN_TEXT);
        assertThat(highlightedComment).isEqualTo(MARKDOWN_TEXT);
    }

    @Test
    public void markdownWithCommitReference_ConvertsItToALink() throws Exception {
        String highlightedComment = markdownToHtml(COMMIT_HASH);
        assertThat(highlightedComment).isEqualTo(
                "<p><a href=\"" + COMMIT_HASH + "\" class=\"vue-enhance\">29e927</a></p>");
    }

    @Test
    public void markdownWithCommitReferenceAndResolver_ConvertsItToALink() throws Exception {
        String highlightedComment = markdownToHtml(COMMIT_HASH, COMMIT_PATH_PREFIX);
        assertThat(highlightedComment).isEqualTo(
                "<p><a href=\"" + COMMIT_PATH_PREFIX + COMMIT_HASH + "\" class=\"vue-enhance\">29e927</a></p>");
    }

    @Test
    public void markdownWithMultipleCommitReferences_ConvertsAllToALink() throws Exception {
        String highlightedComment = markdownToHtml(COMMIT_HASH + " " + COMMIT_HASH);
        assertThat(highlightedComment).isEqualTo(
                "<p><a href=\"" + COMMIT_HASH + "\" class=\"vue-enhance\">29e927</a> <a href=\"" + COMMIT_HASH
                        + "\" class=\"vue-enhance\">29e927</a></p>");
    }

    @Test
    public void markdownWithKnownUserReference_ConvertsItToBold() throws Exception {
        userDao.insert(new User("user@email.com", USER_NAME, "user password"));
        String highlightedComment = markdownToHtml("@" + USER_NAME);
        assertThat(highlightedComment).isEqualTo("<p><strong>@" + USER_NAME + "</strong></p>");
    }

    @Test
    public void markdownWithKnownUserReference_HavingUsersWithSamePrefix_ConvertsItToBold() throws Exception {
        userDao.insert(new User("user1@email.com", USER_NAME, "user password"));
        userDao.insert(new User("user2@email.com", USER_NAME + "-suffix", "user password"));
        String highlightedComment = markdownToHtml("@" + USER_NAME);
        assertThat(highlightedComment).isEqualTo("<p><strong>@" + USER_NAME + "</strong></p>");
    }

    @Test
    public void markdownWithUnknownUserReference_WithoutAnyUsers_LeavesItAsNormalText() throws Exception {
        String highlightedComment = markdownToHtml("@" + USER_NAME);
        assertThat(highlightedComment).isEqualTo("<p>@" + USER_NAME + "</p>");
    }

    @Test
    public void markdownWithUnknownUserReference_WithDifferentUser_LeavesItAsNormalText() throws Exception {
        userDao.insert(new User("user@email.com", OTHER_NAME, "user password"));
        String highlightedComment = markdownToHtml("@" + USER_NAME);
        assertThat(highlightedComment).isEqualTo("<p>@" + USER_NAME + "</p>");
    }

    @Test
    public void markdownWithKnownUserReference_ReturnsMatchedUsers() throws Exception {
        userDao.insert(new User("user@email.com", USER_NAME, "user password"));
        List<User> referencedUsers = service.markdownToHtml("@" + USER_NAME).getReferencedUsers();
        assertThat(referencedUsers).extracting(User::getName).containsExactly(USER_NAME);
    }

    @Test
    public void markdownWithHtml_EscapesHtml() throws Exception {
        assertThat(markdownToHtml(HTML_TEXT)).isEqualTo("<p>" + HTML_TEXT_ESCAPED + "</p>");
    }

    @Test
    public void markdownWithHtml_OnTimeout_EscapesHtml() throws Exception {
        setupServiceTimingOut();
        assertThat(markdownToHtml(HTML_TEXT)).isEqualTo(HTML_TEXT_ESCAPED);
    }

    @Test
    public void markdownWithHtmlInCodeTag_EscapesHtmlOnlyOnce() throws Exception {
        assertThat(markdownToHtml("`<html>`")).isEqualTo("<p><code>&lt;html&gt;</code></p>");
    }

    @Test
    public void markdownWithHtmlInCodeTagWithinLink_EscapesHtmlOnlyOnce() throws Exception {
        assertThat(markdownToHtml("[`<html>`](hostname)")).isEqualTo(
                "<p><a href=\"hostname\"><code>&lt;html&gt;</code></a></p>");
    }

    @Test
    public void markdownWithCodeBlock_InvolvingHtmlTags_EscapesHtmlOnlyOnce() throws Exception {
        assertThat(markdownToHtml("```\n<html>\n```")).isEqualTo("<pre><code>&lt;html&gt;\n</code></pre>");
    }

    @Test
    public void markdownWithCodeBlock_InvolvingBlankLine_RendersCodeBlock() throws Exception {
        assertThat(markdownToHtml("```\ntest\n\ntest\n```")).isEqualTo("<pre><code>test\n\ntest\n</code></pre>");
    }

    @Test
    public void markdownWithLink_RendersHtmlLink() throws Exception {
        assertThat(markdownToHtml("http://google.com/")).isEqualTo(
                "<p><a href=\"http://google.com/\">http://google.com/</a></p>");
    }

    @Test
    public void text_DoesNotUseMarkdown() throws Exception {
        assertThat(textToHtml(MARKDOWN_TEXT)).isEqualTo(MARKDOWN_TEXT);
    }

    @Test
    public void text_OnTimeout_SkipsHighlighting() throws Exception {
        setupServiceTimingOut();
        String highlightedComment = textToHtml(HTML_TEXT);
        assertThat(highlightedComment).isEqualTo(HTML_TEXT_ESCAPED);
    }

    @Test
    public void textWithHtml_EscapesHtml() throws Exception {
        assertThat(textToHtml(HTML_TEXT)).isEqualTo(HTML_TEXT_ESCAPED);
    }

    @Test
    public void textWithCommitReference_ConvertsItToALink() throws Exception {
        String highlightedComment = textToHtml(COMMIT_HASH);
        assertThat(highlightedComment).isEqualTo("<a href=\"" + COMMIT_HASH + "\" class=\"vue-enhance\">29e927</a>");
    }

    @Test
    public void markdownWithKnownUserReference_ConvertsItToBold_AndReturnsMatchedUsers() throws Exception {
        userDao.insert(new User("user@email.com", USER_NAME, "user password"));
        TextHighlightResult highlightResult = service.textToHtml("@" + USER_NAME);
        assertThat(highlightResult.getText()).isEqualTo("<strong>@" + USER_NAME + "</strong>");
        assertThat(highlightResult.getReferencedUsers()).extracting(User::getName).containsExactly(USER_NAME);
    }

    @Test
    public void textWithLink_RendersHtmlLink() throws Exception {
        assertThat(textToHtml("http://google.com/")).isEqualTo("<a href=\"http://google.com/\">http://google.com/</a>");
    }

    @Test
    public void textWithLinkWithFollowingText_RendersHtmlLinkStoppingBeforeText() throws Exception {
        assertThat(textToHtml("http://google.com/ 123")).isEqualTo(
                "<a href=\"http://google.com/\">http://google.com/</a> 123");
    }

    private String markdownToHtml(String markdown) {
        return service.markdownToHtml(markdown).getText();
    }

    private String textToHtml(String markdown) {
        return service.textToHtml(markdown).getText();
    }

    private String markdownToHtml(String markdown, String commitPathPrefix) {
        return service.markdownToHtml(markdown, commitHash -> commitPathPrefix + commitHash).getText();
    }

    private void setupServiceTimingOut() {
        service = new TextHighlightService(() -> -1, userDao);
    }
}
