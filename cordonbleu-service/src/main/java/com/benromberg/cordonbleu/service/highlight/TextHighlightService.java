package com.benromberg.cordonbleu.service.highlight;

import static java.util.Arrays.asList;
import static org.pegdown.Extensions.AUTOLINKS;
import static org.pegdown.Extensions.FENCED_CODE_BLOCKS;
import static org.pegdown.Extensions.HARDWRAPS;
import static org.pegdown.Extensions.STRIKETHROUGH;
import com.benromberg.cordonbleu.data.dao.UserDao;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.ParserRuntimeException;
import org.pegdown.LinkRenderer;
import org.pegdown.Parser;
import org.pegdown.ParsingTimeoutException;
import org.pegdown.PegDownProcessor;
import org.pegdown.Printer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.benromberg.cordonbleu.service.highlight.plugin.CommitReferenceParser;
import com.benromberg.cordonbleu.service.highlight.plugin.CommitReferenceSerializerPlugin;
import com.benromberg.cordonbleu.service.highlight.plugin.UserReferenceParser;
import com.benromberg.cordonbleu.service.highlight.plugin.UserReferenceSerializerPlugin;

public class TextHighlightService {
    private static final int PARSER_EXTENSIONS = HARDWRAPS | AUTOLINKS | STRIKETHROUGH | FENCED_CODE_BLOCKS;
    private static final Logger LOGGER = LoggerFactory.getLogger(TextHighlightService.class);
    private static final Function<String, String> DEFAULT_COMMIT_PATH_RESOLVER = commitHash -> commitHash;
    private final HighlightingTimeout timeout;
    private final UserDao userDao;

    @Inject
    public TextHighlightService(HighlightingTimeout timeout, UserDao userDao) {
        this.timeout = timeout;
        this.userDao = userDao;
    }

    public TextHighlightResult markdownToHtml(String markdown) {
        return markdownToHtml(markdown, DEFAULT_COMMIT_PATH_RESOLVER);
    }

    public TextHighlightResult markdownToHtml(String markdown, Function<String, String> commitUrlResolver) {
        return parseToHtml(markdown, commitUrlResolver, plugins -> new HtmlAlreadyEscapedProcessor(PARSER_EXTENSIONS,
                timeout.getHighlightingTimeoutInMillis(), plugins));
    }

    public TextHighlightResult textToHtml(String text) {
        return parseToHtml(
                text,
                DEFAULT_COMMIT_PATH_RESOLVER,
                plugins -> new TextPegDownProcessor(Parboiled.createParser(TextParser.class,
                        timeout.getHighlightingTimeoutInMillis(), plugins)));
    }

    private TextHighlightResult parseToHtml(String input, Function<String, String> commitUrlResolver,
            Function<PegDownPlugins, PegDownProcessor> processorFactory) {
        String inputWithoutHtml = StringEscapeUtils.escapeHtml4(input);
        try {
            return parseWithTimeout(inputWithoutHtml, commitUrlResolver, processorFactory);
        } catch (ParserRuntimeException e) {
            if (e.getCause() instanceof ParsingTimeoutException) {
                LOGGER.info("Interrupted highlighting of {}.", commitUrlResolver);
                return new TextHighlightResult(inputWithoutHtml, asList());
            }
            throw e;
        }
    }

    private TextHighlightResult parseWithTimeout(String input, Function<String, String> commitUrlResolver,
            Function<PegDownPlugins, PegDownProcessor> processorFactory) {
        UserReferenceSerializerPlugin userReferenceSerializer = new UserReferenceSerializerPlugin();
        PegDownPlugins plugins = PegDownPlugins.builder().withPlugin(CommitReferenceParser.class)
                .withHtmlSerializer(new CommitReferenceSerializerPlugin(commitUrlResolver))
                .withPlugin(UserReferenceParser.class, userDao.findAll()).withHtmlSerializer(userReferenceSerializer)
                .build();
        PegDownProcessor processor = processorFactory.apply(plugins);
        return new TextHighlightResult(processor.markdownToHtml(input), userReferenceSerializer.getUsers());
    }

    private static class TextParser extends Parser {
        public TextParser(Long maxParsingTimeInMillis, PegDownPlugins plugins) {
            super(PARSER_EXTENSIONS, maxParsingTimeInMillis, Parser.DefaultParseRunnerProvider, plugins);
        }

        @Override
        public Rule Root() {
            Rule[] rules = new Rule[plugins.getInlinePluginRules().length + 2];
            for (int i = 0; i < plugins.getInlinePluginRules().length; i++) {
                rules[i] = plugins.getInlinePluginRules()[i];
            }
            rules[plugins.getInlinePluginRules().length] = AutoLink();
            rules[plugins.getInlinePluginRules().length + 1] = Sequence(ANY, push(new TextNode(match())));
            return NodeSequence(push(new RootNode()), ZeroOrMore(FirstOf(rules), addAsChild()));
        }
    }

    private static class TextPegDownProcessor extends HtmlAlreadyEscapedProcessor {
        public TextPegDownProcessor(Parser parser) {
            super(parser);
        }

        @Override
        public char[] prepareSource(char[] source) {
            return source;
        }
    }

    private static class HtmlAlreadyEscapedProcessor extends PegDownProcessor {
        public HtmlAlreadyEscapedProcessor(int options, long maxParsingTimeInMillis, PegDownPlugins plugins) {
            super(options, maxParsingTimeInMillis, plugins);
        }

        public HtmlAlreadyEscapedProcessor(Parser parser) {
            super(parser);
        }

        @Override
        public String markdownToHtml(char[] markdownSource, LinkRenderer linkRenderer,
                Map<String, VerbatimSerializer> verbatimSerializerMap, List<ToHtmlSerializerPlugin> plugins) {
            try {
                RootNode astRoot = parseMarkdown(markdownSource);
                return new HtmlAlreadyEscapedToHtmlSerializer(linkRenderer, verbatimSerializerMap, plugins)
                        .toHtml(astRoot);
            } catch (ParsingTimeoutException e) {
                return null;
            }
        }
    }

    private static class HtmlAlreadyEscapedToHtmlSerializer extends ToHtmlSerializer {
        public HtmlAlreadyEscapedToHtmlSerializer(LinkRenderer linkRenderer,
                Map<String, VerbatimSerializer> verbatimSerializerMap, List<ToHtmlSerializerPlugin> plugins) {
            super(linkRenderer, verbatimSerializerMap, plugins);
            printer = new NonEncodingPrinter();
        }

        @Override
        protected String printChildrenToString(SuperNode node) {
            Printer priorPrinter = printer;
            printer = new NonEncodingPrinter();
            visitChildren(node);
            String result = printer.getString();
            printer = priorPrinter;
            return result;
        }
    }

    private static class NonEncodingPrinter extends Printer {
        @Override
        public Printer printEncoded(String string) {
            return super.print(string);
        }
    }
}
