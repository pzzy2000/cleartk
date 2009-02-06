 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
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
package org.cleartk.syntax.treebank;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.cleartk.syntax.treebank.TreebankGoldAnnotator;
import org.cleartk.syntax.treebank.type.TopTreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.util.AnnotationRetrieval;
import org.cleartk.util.DocumentUtil;
import org.cleartk.util.TestsUtil;
import org.junit.Test;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Philip Ogren
 *
 */
public class TreebankGoldReaderAndAnnotatorTests {
	
	@Test
	public void craftTest1() throws Exception {
		String treebankParse = "( (X (NP (NP (NML (NN Complex ) (NN trait )) (NN analysis )) (PP (IN of ) (NP (DT the ) (NN mouse ) (NN striatum )))) (: : ) (S (NP-SBJ (JJ independent ) (NNS QTLs )) (VP (VBP modulate ) (NP (NP (NN volume )) (CC and ) (NP (NN neuron ) (NN number)))))) )";
		String expectedText = "Complex trait analysis of the mouse striatum: independent QTLs modulate volume and neuron number";

		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				TreebankGoldAnnotator.class, TestsUtil.getTypeSystem("desc/TypeSystem.xml"));
		TreebankGoldAnnotator treebankGoldAnnotator = new TreebankGoldAnnotator();
		treebankGoldAnnotator.initialize(engine.getUimaContext());
		
		JCas jCas = engine.newJCas();
		JCas tbView = jCas.createView("TreebankView");
		tbView.setDocumentText(treebankParse);
		DocumentUtil.createDocument(tbView, "test-id", "test-path");
		
		treebankGoldAnnotator.process(jCas);
		
		JCas goldView = jCas.getView("GoldView");
		
		FSIndex sentenceIndex = goldView.getAnnotationIndex(Sentence.type);
		assertEquals(1, sentenceIndex.size());
		
		Sentence firstSentence = AnnotationRetrieval.get(goldView, Sentence.class, 0);
		assertEquals(expectedText, firstSentence.getCoveredText());
		
		FSIndex topNodeIndex = goldView.getAnnotationIndex(TopTreebankNode.type);
		TopTreebankNode topNode = (TopTreebankNode) topNodeIndex.iterator().next();
		
		int i = 0;
		assertEquals("Complex", topNode.getTerminals(i++).getCoveredText());
		assertEquals("trait", topNode.getTerminals(i++).getCoveredText());
		assertEquals("analysis", topNode.getTerminals(i++).getCoveredText());
		assertEquals("of", topNode.getTerminals(i++).getCoveredText());
		assertEquals("the", topNode.getTerminals(i++).getCoveredText());
		assertEquals("mouse", topNode.getTerminals(i++).getCoveredText());
		assertEquals("striatum", topNode.getTerminals(i++).getCoveredText());
		assertEquals(":", topNode.getTerminals(i++).getCoveredText());
		assertEquals("independent", topNode.getTerminals(i++).getCoveredText());
		assertEquals("QTLs", topNode.getTerminals(i++).getCoveredText());
		assertEquals("modulate", topNode.getTerminals(i++).getCoveredText());
		assertEquals("volume", topNode.getTerminals(i++).getCoveredText());
		assertEquals("and", topNode.getTerminals(i++).getCoveredText());
		assertEquals("neuron", topNode.getTerminals(i++).getCoveredText());
		assertEquals("number", topNode.getTerminals(i++).getCoveredText());
	}
	
	
	@Test
	public void testAnnotatorDescriptor() throws UIMAException, IOException {
		AnalysisEngine engine = TestsUtil.getAnalysisEngine(
				"desc/syntax/treebank/TreebankGoldAnnotator.xml");
		engine.collectionProcessComplete();
	}
	

}
