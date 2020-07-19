/*
 * Copyright Gunnar Morling
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package dev.morling.quarkus.extensions.lucene.extension;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.TermFrequencyAttribute;
import org.apache.lucene.analysis.tokenattributes.TermFrequencyAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttributeImpl;
import org.apache.lucene.search.BoostAttribute;
import org.apache.lucene.search.BoostAttributeImpl;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * An AttributeFactory creates instances of {@link AttributeImpl}s.
 */
@TargetClass(className = "org.apache.lucene.util.AttributeFactory$StaticImplementationAttributeFactory")
public final class StaticImplementationAttributeFactorySubstitution {

    public StaticImplementationAttributeFactorySubstitution() {}

    @Substitute
    public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
        if (attClass == BoostAttribute.class) {
            return new BoostAttributeImpl();
        }
        else if (attClass == CharTermAttribute.class) {
            return new CharTermAttributeImpl();
        }
        else if (attClass == OffsetAttribute.class) {
            return new OffsetAttributeImpl();
        }
        else if (attClass == PositionIncrementAttribute.class) {
            return new PositionIncrementAttributeImpl();
        }
        else if (attClass == TypeAttribute.class) {
            return new TypeAttributeImpl();
        }
        else if (attClass == TermFrequencyAttribute.class) {
            return new TermFrequencyAttributeImpl();
        }
        else if (attClass == PayloadAttribute.class) {
            return new PayloadAttributeImpl();
        }
        else if (attClass == PositionLengthAttribute.class) {
            return new PositionLengthAttributeImpl();
        }
        else if (attClass == KeywordAttribute.class) {
            return new KeywordAttributeImpl();
        }

        throw new UnsupportedOperationException("Unknown: " + attClass);
    }
}
