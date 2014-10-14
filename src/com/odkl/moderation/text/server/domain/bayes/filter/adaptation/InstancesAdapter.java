package com.odkl.moderation.text.server.domain.bayes.filter.adaptation;

import com.odkl.moderation.text.server.domain.bayes.utils.Dictionary;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;

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

        public void addAllAttributes() {
                Dictionary dictionary = Dictionary.getInstance();
                int nGramCount = dictionary.getDictionarySize();

                FastVector attributes = new FastVector(nGramCount + 1);

                FastVector fvClassVal = new FastVector(2);
                fvClassVal.addElement("ham");
                fvClassVal.addElement("spam");
                Attribute classAttribute = new Attribute("@@class@@", fvClassVal, 0);
                attributes.addElement(classAttribute);

                int i = 1;
                for (String base : dictionary.getBaseSet()) {
                        attributes.addElement(new Attribute(base, i++));
                }

                instances.replaceAttributes(attributes);
                instances.setClassIndex(0);
        }
}
