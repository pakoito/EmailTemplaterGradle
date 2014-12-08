
package com.coolchick.translatortemplater.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "translators",
    "all-languages"
})
public class TranslatorDatabase {

    @JsonProperty("translators")
    private List<Translator> translators = new ArrayList<Translator>();
    @JsonProperty("all-languages")
    private List<String> allLanguages = new ArrayList<String>();
//    @JsonIgnore
//    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The translators
     */
    @JsonProperty("translators")
    public List<Translator> getTranslators() {
        return translators;
    }

    /**
     * 
     * @param translators
     *     The translators
     */
    @JsonProperty("translators")
    public void setTranslators(List<Translator> translators) {
        this.translators = translators;
    }

    public TranslatorDatabase withTranslators(List<Translator> translators) {
        this.translators = translators;
        return this;
    }

    /**
     *
     * @return
     * The allLanguages
     */
    @JsonProperty("all-languages")
    public List<String> getAllLanguages() {
        return allLanguages;
    }

    /**
     *
     * @param allLanguages
     * The all-languages
     */
    @JsonProperty("all-languages")
    public void setAllLanguages(List<String> allLanguages) {
        this.allLanguages = allLanguages;
    }

    public TranslatorDatabase withAllLanguages(List<String> allLanguages) {
        this.allLanguages = allLanguages;
        return this;
    }

//    @JsonAnyGetter
//    public Map<String, Object> getAdditionalProperties() {
//        return this.additionalProperties;
//    }
//
//    @JsonAnySetter
//    public void setAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//    }
//
//    public TranslatorDatabase withAdditionalProperty(String name, Object value) {
//        this.additionalProperties.put(name, value);
//        return this;
//    }

}
