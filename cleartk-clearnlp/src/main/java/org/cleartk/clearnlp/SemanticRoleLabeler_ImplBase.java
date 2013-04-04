/*
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.clearnlp;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPDecode;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP semantic role labeler. A typical
 * pipeline preceding this analysis engine would consist of a tokenizer, sentence segmenter,
 * POS tagger, lemmatizer (mp analyzer), and dependency parser.
 * <p>
 * The ClearNLP labeler is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
* 
 * @author Lee Becker
 * 
 */
public abstract class SemanticRoleLabeler_ImplBase<
    TOKEN_TYPE extends Annotation,
    DEPENDENCY_NODE_TYPE extends Annotation,
    TOP_DEPENDENCY_NODE_TYPE extends DEPENDENCY_NODE_TYPE,
    DEPENDENCY_RELATION_TYPE extends FeatureStructure,
    SEMANTIC_ARGUMENT_TYPE extends Annotation,
    PREDICATE_TYPE extends Annotation>
  extends JCasAnnotator_ImplBase {


  public static final String DEFAULT_PRED_ID_MODEL_FILE_NAME = "ontonotes-en-pred-1.3.0.tgz";
  public static final String DEFAULT_ROLESET_MODEL_FILE_NAME = "ontonotes-en-role-1.3.0.tgz";
  public static final String DEFAULT_SRL_MODEL_FILE_NAME = "ontonotes-en-srl-1.3.0.tgz";

  public static final String PARAM_SRL_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler_ImplBase.class, 
      "srlModelUri");

  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the semantic role labeler model.  If none is specified it will use the default ontonotes model.")
  private URI srlModelUri;

  public static final String PARAM_PRED_ID_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler_ImplBase.class,
      "predIdModelUri");
  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the predicate identifier model.  If none is specified it will use the default ontonotes model.")
  private URI predIdModelUri;
  
  public static final String PARAM_ROLESET_MODEL_URI = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler_ImplBase.class,
      "rolesetModelUri");

  @ConfigurationParameter(
      description = "This parameter provides the URI pointing to the role set classifier model.  If none is specified it will use the default ontonotes model.")
  private URI rolesetModelUri;

 
  public static final String PARAM_LANGUAGE_CODE = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler_ImplBase.class,
      "languageCode");
  
  @ConfigurationParameter(
      description = "Language code for the semantic role labeler (default value=en).",
      defaultValue= AbstractReader.LANG_EN)
  private String languageCode;
  
  public static final String PARAM_WINDOW_CLASS = ConfigurationParameterFactory.createConfigurationParameterName(
      SemanticRoleLabeler_ImplBase.class,
      "windowClass");

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private Class<? extends Annotation> windowClass;


  @Override
  public void initialize(UimaContext aContext)
      throws ResourceInitializationException {
    super.initialize(aContext);

    try {
      URL predIdModelURL = (this.predIdModelUri == null) 
          ? SemanticRoleLabeler_ImplBase.class.getResource(DEFAULT_PRED_ID_MODEL_FILE_NAME).toURI().toURL() 
          : this.predIdModelUri.toURL();
      this.predIdentifier = EngineGetter.getComponent(predIdModelURL.openStream(), languageCode, NLPLib.MODE_PRED);
      
      URL rolesetModelUrl = (this.rolesetModelUri == null)
          ? SemanticRoleLabeler_ImplBase.class.getResource(DEFAULT_ROLESET_MODEL_FILE_NAME).toURI().toURL()
          : this.rolesetModelUri.toURL();
      this.roleSetClassifier = EngineGetter.getComponent(rolesetModelUrl.openStream(), languageCode, NLPLib.MODE_ROLE);

      URL srlModelURL = (this.srlModelUri == null) 
          ? SemanticRoleLabeler_ImplBase.class.getResource(DEFAULT_SRL_MODEL_FILE_NAME).toURI().toURL() 
          : this.srlModelUri.toURL();
      this.srlabeler = EngineGetter.getComponent(srlModelURL.openStream(), languageCode, NLPLib.MODE_SRL);
      
      this.clearNlpDecoder = new NLPDecode();
      


    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  /**
   * Convenience method for creating Analysis Engine for ClearNLP's dependency parser using default English model files
   */
  public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
    return AnalysisEngineFactory.createPrimitiveDescription(SemanticRoleLabeler_ImplBase.class);

  }

  
  protected abstract TokenOps<TOKEN_TYPE> getTokenOps();
  
  protected abstract DependencyOps<DEPENDENCY_NODE_TYPE, TOP_DEPENDENCY_NODE_TYPE, DEPENDENCY_RELATION_TYPE, TOKEN_TYPE> getDependencyOps();
  
  protected abstract SrlOps<SEMANTIC_ARGUMENT_TYPE, PREDICATE_TYPE, TOKEN_TYPE> getSrlOps();

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    
    for (Annotation window : JCasUtil.select(jCas, this.windowClass)) {
      boolean skipSentence = false;
      List<TOKEN_TYPE> tokens = this.getTokenOps().selectTokens(jCas, window);
      List<String> tokenStrings = JCasUtil.toText(tokens);

      // Build dependency tree from token information
      DEPTree tree = clearNlpDecoder.toDEPTree(tokenStrings);
      //DEPTree tree = new DEPTree();
      for (int i = 1; i < tree.size(); i++) {
        TOKEN_TYPE token = tokens.get(i-1);
        DEPNode node = tree.get(i);
        node.pos = this.getTokenOps().getPos(jCas, token);
        node.lemma = this.getTokenOps().getLemma(jCas, token);
      }

      // Build map between CAS dependency node and id for later creation of
      // ClearParser dependency node/tree
      Map<DEPENDENCY_NODE_TYPE, Integer> depNodeToID = Maps.newHashMap();
      int nodeId = 1;
      for (DEPENDENCY_NODE_TYPE depNode : this.getDependencyOps().selectDependencyNodes(jCas, window)) {
        if (this.getDependencyOps().isTopNode(jCas, depNode)) {
          depNodeToID.put(depNode, 0);
        } else {
          depNodeToID.put(depNode, nodeId);
          nodeId++;
        }
      }
      
      // Initialize Dependency Relations for ClearNLP input
      for (int i = 0; i < tokens.size(); i++) {
        TOKEN_TYPE token = tokens.get(i);

        // Determine node and head
        DEPENDENCY_NODE_TYPE casDepNode = this.getDependencyOps().getDependencyNode(jCas, token);
        if (this.getDependencyOps().hasHeadRelation(jCas, casDepNode)) {
          DEPENDENCY_NODE_TYPE head = this.getDependencyOps().getHead(jCas, casDepNode);

          int id = i + 1;
          DEPNode node = tree.get(id);

          int headId = depNodeToID.get(head);
          DEPNode headNode = tree.get(headId);
          node.setHead(headNode, this.getDependencyOps().getHeadRelation(jCas, casDepNode));
        } else {
          // In cases where the sentence is unparseable we are left with only a root node
          // Thus the Semantic Role Labeler should skip this sentence
          skipSentence = true;
        }
      }
      
      // Run the SRL
      if (!skipSentence) {
        this.predIdentifier.process(tree);
        this.roleSetClassifier.process(tree);
        this.srlabeler.process(tree);
        
        // Extract SRL information and create ClearTK CAS types
        this.extractSRLInfo(jCas, tokens, tree);
      }
    }
  }


  /**
   * Converts the output from the ClearParser Semantic Role Labeler to the ClearTK Predicate and
   * SemanticArgument Types.
   * 
   * @param jCas
   * @param tokens
   *          - In order list of tokens
   * @param tree
   *          - DepdendencyTree output by ClearParser SRLPredict
   */
  private void extractSRLInfo(JCas jCas, List<TOKEN_TYPE> tokens, DEPTree tree) {
    Map<Integer, PREDICATE_TYPE> headIdToPredicate = Maps.newHashMap();
    Map<PREDICATE_TYPE, List<SEMANTIC_ARGUMENT_TYPE>> predicateArguments = Maps.newHashMap();

    // Start at node 1, since node 0 is considered the head of the sentence
    for (int i = 1; i < tree.size(); i++) {
      // Every ClearParser parserNode will contain an srlInfo field.
      DEPNode parserNode = tree.get(i);
      TOKEN_TYPE token = tokens.get(i - 1);
      
      List<DEPArc> semanticHeads = parserNode.getSHeads();
      if (semanticHeads.isEmpty()) { continue; }
      
      // Parse semantic head relations to get SRL triplets
      for (DEPArc shead : semanticHeads) {
        int headId = shead.getNode().id;
        TOKEN_TYPE headToken = tokens.get(headId - 1);
        PREDICATE_TYPE pred;
        List<SEMANTIC_ARGUMENT_TYPE> args;
        if (!headIdToPredicate.containsKey(headId)) {
          String rolesetId = shead.getNode().getFeat(DEPLib.FEAT_PB);
          pred = this.getSrlOps().createPredicate(jCas, rolesetId, headToken);
          headIdToPredicate.put(headId, pred);
          args = Lists.newArrayList();
          predicateArguments.put(pred, args);
        } else {
          pred = headIdToPredicate.get(headId);
          args = predicateArguments.get(pred);
        }
        args.add(this.getSrlOps().createArgument(jCas, shead, token));
      }
    }    
    
    // Store Arguments in Predicate
    for (Map.Entry<PREDICATE_TYPE, List<SEMANTIC_ARGUMENT_TYPE>> entry : predicateArguments.entrySet()) {
      PREDICATE_TYPE predicate = entry.getKey();
      List<SEMANTIC_ARGUMENT_TYPE> arguments = entry.getValue();
      this.getSrlOps().setPredicateArguments(jCas, predicate, arguments);
    }
    
      
  }

  private AbstractComponent predIdentifier;
  private AbstractComponent roleSetClassifier;
  private AbstractComponent srlabeler;
  private NLPDecode clearNlpDecoder;
}
