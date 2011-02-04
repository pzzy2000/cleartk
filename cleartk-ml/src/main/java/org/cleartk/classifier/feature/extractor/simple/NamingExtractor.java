/** 
 * Copyright (c) 2007-2009, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.classifier.feature.extractor.simple;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;

/**
 * <br>
 * Copyright (c) 2007-2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philipp Wetzler
 */
public class NamingExtractor implements SimpleFeatureExtractor {

  public NamingExtractor(String name, SimpleFeatureExtractor subExtractor) {
    this.name = name;
    this.subExtractor = subExtractor;
  }

  public NamingExtractor(String name, SimpleFeatureExtractor... subExtractors) {
    this(name, new CombinedExtractor(subExtractors));
  }

  public List<Feature> extract(JCas view, Annotation focusAnnotation)
      throws CleartkExtractorException {
    List<Feature> features = subExtractor.extract(view, focusAnnotation);

    for (Feature feature : features) {
      feature.setName(Feature.createName(name, feature.getName()));
    }

    return features;
  }

  private String name;

  private SimpleFeatureExtractor subExtractor;

}
