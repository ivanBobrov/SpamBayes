package com.odkl.moderation.text.server.domain.bayes.filter.adaptation;

import java.io.Serializable;

import com.odkl.moderation.text.server.domain.bayes.utils.Dictionary;

import gnu.trove.procedure.TIntProcedure;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class InstancesAdapter implements Serializable {
    private final FastInstances instances;

    public InstancesAdapter() {
        FastVector attributes = new FastVector(1);

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("ham");
        fvClassVal.addElement("spam");

        Attribute classAttribute = new Attribute("@@class@@", fvClassVal);
        attributes.addElement(classAttribute);

        instances = new FastInstances("Relation", attributes, 1);
        instances.setClassIndex(0);
    }

    public void add(Instance instance) {
        instances.add(instance);
    }

    public void removeLastInstance() {
        instances.delete(instances.numInstances() - 1);
    }

    public Instances getRawInstances() {
        return instances;
    }

    public void addAllAttributes(Dictionary dictionary) {
        int nGramCount = dictionary.getDictionarySize();

        final FastVector attributes = new FastVector(nGramCount + 1);

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("ham");
        fvClassVal.addElement("spam");

        Attribute classAttribute = new Attribute("@@class@@", fvClassVal, 0);
        attributes.addElement(classAttribute);

        dictionary.getBaseSet().forEach(new TIntProcedure() {

            private int i = 1;

            @Override
            public boolean execute(int value) {
                attributes.addElement(new Attribute(Long.toString(value), i++));
                return true;
            }
        });

        instances.replaceAttributes(attributes);
        instances.setClassIndex(0);
    }
}
