/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme;


import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
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


        throw new UnsupportedOperationException("Unknown: " + attClass);
    }
}
