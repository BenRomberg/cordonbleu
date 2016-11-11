from pygments import highlight
from pygments.lexer import RegexLexer
from pygments.token import *
from pygments.lexers import guess_lexer_for_filename
from pygments.formatters import HtmlFormatter
from pygments.util import ClassNotFound

class DefaultLexer(RegexLexer):
    tokens = {
        'root': [
            (r'.*\n', Text),
        ]
    }

class NakedHtmlFormatter(HtmlFormatter):
    def wrap(self, source, outfile):
        return source

try:
    lexer = guess_lexer_for_filename(path, source, stripnl=False)
except ClassNotFound:
    lexer = DefaultLexer()

result = highlight(source, lexer, NakedHtmlFormatter())
