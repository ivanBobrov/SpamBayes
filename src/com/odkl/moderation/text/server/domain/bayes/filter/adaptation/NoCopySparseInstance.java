package com.odkl.moderation.text.server.domain.bayes.filter.adaptation;

import weka.core.SparseInstance;

public class NoCopySparseInstance extends SparseInstance {

    /**
     * @see weka.core.SparseInstance#SparseInstance(double, double[], int[], int)
     */
    public NoCopySparseInstance(double weight, double[] attValues,
            int[] indices, int maxNumValues){
        m_AttValues = attValues;
        m_Indices = indices;
        m_Weight = weight;
        m_NumAttributes = maxNumValues;
        m_Dataset = null;
    }

}
