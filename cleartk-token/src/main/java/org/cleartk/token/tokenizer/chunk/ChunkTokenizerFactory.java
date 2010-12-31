/** 
  * Copyright (c) 2009, Regents of the University of Colorado 
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

package org.cleartk.token.tokenizer.chunk;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.chunker.Chunker;
import org.cleartk.classifier.CleartkComponents;
import org.cleartk.classifier.CleartkSequentialAnnotator;
import org.cleartk.classifier.jar.JarClassifierFactory;
import org.cleartk.classifier.jar.JarSequentialDataWriterFactory;
import org.cleartk.classifier.mallet.DefaultMalletCRFDataWriterFactory;
import org.cleartk.token.TokenComponents;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Subtoken;
import org.cleartk.token.type.Token;

/**
 * <br>Copyright (c) 2009, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class ChunkTokenizerFactory {

	public static AnalysisEngineDescription createChunkTokenizer(String modelFileName) throws ResourceInitializationException {
		List<Class<?>> dynamicallyLoadedClasses = new ArrayList<Class<?>>();
		dynamicallyLoadedClasses.add(Chunker.class);
		dynamicallyLoadedClasses.add(ChunkTokenizerLabeler.class);
		
		return CleartkComponents.createCleartkSequentialAnnotator(Chunker.class,
				TokenComponents.TYPE_SYSTEM_DESCRIPTION,
				null, 
				dynamicallyLoadedClasses,
			JarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelFileName,
			Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME, Subtoken.class.getName(),
			Chunker.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
			Chunker.PARAM_CHUNK_LABELER_CLASS_NAME, ChunkTokenizerLabeler.class.getName(),
			Chunker.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, ChunkTokenizerFeatureExtractor.class.getName(),
			ChunkTokenizerLabeler.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Token.class.getName()
		);
	}
	
	public static AnalysisEngineDescription createChunkTokenizerDataWriter(String outputDirectoryName) throws ResourceInitializationException {
		List<Class<?>> dynamicallyLoadedClasses = new ArrayList<Class<?>>();
		dynamicallyLoadedClasses.add(Chunker.class);
		dynamicallyLoadedClasses.add(ChunkTokenizerLabeler.class);
		
		return CleartkComponents.createCleartkSequentialAnnotator(Chunker.class,
				TokenComponents.TYPE_SYSTEM_DESCRIPTION,
				null, 
				dynamicallyLoadedClasses,
				JarSequentialDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDirectoryName,
				CleartkSequentialAnnotator.PARAM_SEQUENTIAL_DATA_WRITER_FACTORY_CLASS_NAME, DefaultMalletCRFDataWriterFactory.class.getName(),
			Chunker.PARAM_LABELED_ANNOTATION_CLASS_NAME, Subtoken.class.getName(),
			Chunker.PARAM_SEQUENCE_CLASS_NAME, Sentence.class.getName(),
			Chunker.PARAM_CHUNK_LABELER_CLASS_NAME, ChunkTokenizerLabeler.class.getName(),
			Chunker.PARAM_CHUNKER_FEATURE_EXTRACTOR_CLASS_NAME, ChunkTokenizerFeatureExtractor.class.getName(),
			ChunkTokenizerLabeler.PARAM_CHUNK_ANNOTATION_CLASS_NAME, Token.class.getName()
		);
	}

}
