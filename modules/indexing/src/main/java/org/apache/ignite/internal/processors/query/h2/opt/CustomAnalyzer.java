package org.apache.ignite.internal.processors.query.h2.opt;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * copy of https://github.com/apache/lucene-solr/blob/branch_7_4/lucene/core/src/java/org/apache/lucene/analysis/standard/StandardAnalyzer.java
 * except this version is with removed stopwords so all words are indexed
 */
public final class CustomAnalyzer extends StopwordAnalyzerBase {
    
    /** An unmodifiable set containing some common English words that are not usually useful
     for searching.*/
    public static final CharArraySet ENGLISH_STOP_WORDS_SET = CharArraySet.EMPTY_SET;
    
    /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    
    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
    
    /** An unmodifiable set containing some common English words that are usually not
     useful for searching. */
    public static final CharArraySet STOP_WORDS_SET = ENGLISH_STOP_WORDS_SET;
    
    /** Builds an analyzer with the given stop words.
     * @param stopWords stop words */
    public CustomAnalyzer(CharArraySet stopWords) {
        super(stopWords);
    }
    
    /** Builds an analyzer with the default stop words ({@link #STOP_WORDS_SET}).
     */
    public CustomAnalyzer() {
        this(STOP_WORDS_SET);
    }
    
    /** Builds an analyzer with the stop words from the given reader.
     * @see WordlistLoader#getWordSet(Reader)
     * @param stopwords Reader to read stop words from */
    public CustomAnalyzer(Reader stopwords) throws IOException {
        this(loadStopwordSet(stopwords));
    }
    
    /**
     * Set the max allowed token length.  Tokens larger than this will be chopped
     * up at this token length and emitted as multiple tokens.  If you need to
     * skip such large tokens, you could increase this max length, and then
     * use {@code LengthFilter} to remove long tokens.  The default is
     * {@link CustomAnalyzer#DEFAULT_MAX_TOKEN_LENGTH}.
     */
    public void setMaxTokenLength(int length) {
        maxTokenLength = length;
    }
    
    /** Returns the current maximum token length
     *
     *  @see #setMaxTokenLength */
    public int getMaxTokenLength() {
        return maxTokenLength;
    }
    
    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final StandardTokenizer src = new StandardTokenizer();
        src.setMaxTokenLength(maxTokenLength);
        TokenStream tok = new StandardFilter(src);
        tok = new LowerCaseFilter(tok);
        tok = new StopFilter(tok, stopwords);
        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) {
                // So that if maxTokenLength was changed, the change takes
                // effect next time tokenStream is called:
                src.setMaxTokenLength(CustomAnalyzer.this.maxTokenLength);
                super.setReader(reader);
            }
        };
    }
    
    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new StandardFilter(in);
        result = new LowerCaseFilter(result);
        return result;
    }
}