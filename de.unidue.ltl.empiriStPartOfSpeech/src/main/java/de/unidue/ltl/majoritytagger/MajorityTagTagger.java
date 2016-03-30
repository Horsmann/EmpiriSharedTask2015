package de.unidue.ltl.majoritytagger;

/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MajorityTagTagger
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    public static final String PARAM_DEFAULT_TAG = "PARAM_DEFAULT_TAG";
    @ConfigurationParameter(name = PARAM_DEFAULT_TAG, mandatory = false, defaultValue = "NN")
    protected String defaultTag;

    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;

    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    private String posMappingLocation;

    /**
     * Override the default group id to use components which are not in the group of in
     * de.tudarmstadt.ukp.core
     */
    public static final String PARAM_GROUP_ID = "PARAM_GROUP_ID";
    @ConfigurationParameter(name = PARAM_GROUP_ID, mandatory = true, defaultValue = "de.unidue.ltl")
    protected String groupid;

    private ModelProviderBase<File> modelProvider = null;
    private MappingProvider mappingProvider = null;


    Map<String, String> cluster = new HashMap<String, String>();
    Dictionary loader;

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        initModelProvider();

        File resource = modelProvider.getResource();

        mappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                language, modelProvider);

        try {
            loader = loadDictionary(resource);
        }
        catch (Exception e) {
         throw new ResourceInitializationException(e);
        }
    }

    protected Dictionary loadDictionary(File resource)
        throws Exception
    {
        return new Dictionary(defaultTag, resource.getAbsolutePath() + "/" + "mft.map");
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        mappingProvider.configure(aJCas.getCas());

        Collection<Sentence> select = JCasUtil.select(aJCas, Sentence.class);
        for (Sentence sentence : select) {
            List<Token> selectCovered = JCasUtil.selectCovered(aJCas, Token.class,
                    sentence.getBegin(), sentence.getEnd());
            for (int i = 0; i < selectCovered.size(); i++) {
                String token = selectCovered.get(i).getCoveredText();
                String tag = loader.getTag(token);

                int beg = selectCovered.get(i).getBegin();
                int end = selectCovered.get(i).getEnd();

                Type posTag = mappingProvider.getTagType(tag);
                POS posAnno = (POS) aJCas.getCas().createAnnotation(posTag, beg, end);
                posAnno.setPosValue(tag);
                posAnno.addToIndexes();

                selectCovered.get(i).setPos(posAnno);
            }
        }
    }

    private void initModelProvider()
        throws ResourceInitializationException
    {
        modelProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(MajorityTagTagger.this);
                setOverride(GROUP_ID, groupid);
                setDefault(ARTIFACT_ID, "${groupId}.majoritytagger-model-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/${package}/lib/tagger-${language}-${variant}.properties");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl)
                throws IOException
            {
                File folder = ResourceUtils.getClasspathAsFolder(aUrl.toString(), true);
                return folder;
            }
        };
        try {
            modelProvider.configure();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
}
