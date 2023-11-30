/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.simulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPA entity for representing a scenario parameter
 */
@Entity
public class ScenarioParameter extends AbstractAuditingEntity<ScenarioParameter, Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parameterId;

    @NotEmpty
    @Column(nullable = false, updatable = false)
    private String name;

    /**
     * Actual control type as a numerical representation of {@link ControlType}
     */
    @NotNull
    @Column(nullable = false, updatable = false)
    private Integer controlType = ControlType.UNKNOWN.getId();

    @Lob
    @NotEmpty
    @Column(columnDefinition = "CLOB", name = "parameter_value", nullable = false, updatable = false)
    private String value;

    @ManyToOne
    @JsonIgnoreProperties(value = {"scenarioParameters", "scenarioActions", "scenarioMessages"}, allowSetters = true)
    private ScenarioExecution scenarioExecution;

    private boolean required;

    private String label;

    @Transient
    private List<ScenarioParameterOption> options = new ArrayList<>();

    public static ScenarioParameterBuilder builder() {
        return new ScenarioParameterBuilder();
    }

    public Long getParameterId() {
        return parameterId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<ScenarioParameterOption> getOptions() {
        return options;
    }

    public void setOptions(List<ScenarioParameterOption> options) {
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ControlType getControlType() {
        return ControlType.fromId(controlType);
    }

    public void setControlType(ControlType controlType) {
        this.controlType = controlType.id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ScenarioExecution getScenarioExecution() {
        return scenarioExecution;
    }

    public void setScenarioExecution(ScenarioExecution scenarioExecution) {
        this.scenarioExecution = scenarioExecution;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return "ScenarioParameter{" +
            ", parameterId='" + getParameterId() + "'" +
            ", name='" + getName() + "'" + "'" +
            ", controlType='" + getControlType() + "'" +
            ", value='" + getValue() + "'" +
            ", required='" + isRequired() + "'" +
            ", label='" + getLabel() + "'" +
            ", createdDate='" + getCreatedDate() + "'" +
            ", lastModifiedDate='" + getLastModifiedDate() + "'" +
            "}";
    }

    public enum ControlType {

        UNKNOWN(0), TEXTBOX(1), TEXTAREA(2), DROPDOWN(3);

        private final int id;

        ControlType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ControlType fromId(int id) {
            return Arrays.stream(values())
                .filter(controlType -> controlType.id == id)
                .findFirst()
                .orElse(ControlType.UNKNOWN);
        }
    }

    public static class ScenarioParameterBuilder extends AuditingEntityBuilder<ScenarioParameterBuilder, ScenarioParameter, Long> {

        private final ScenarioParameter scenarioParameter = new ScenarioParameter();

        @Override
        protected ScenarioParameter getEntity() {
            return scenarioParameter;
        }

        public ScenarioParameterBuilder name(String name) {
            scenarioParameter.name = name;
            return this;
        }

        public ScenarioParameterBuilder controlType(ControlType controlType) {
            scenarioParameter.controlType = controlType.getId();
            return this;
        }

        public ScenarioParameterBuilder textbox() {
            scenarioParameter.controlType = ScenarioParameter.ControlType.TEXTBOX.getId();
            return this;
        }

        public ScenarioParameterBuilder textarea() {
            scenarioParameter.controlType = ScenarioParameter.ControlType.TEXTAREA.getId();
            return this;
        }

        public ScenarioParameterBuilder dropdown() {
            scenarioParameter.controlType = ScenarioParameter.ControlType.DROPDOWN.getId();
            return this;
        }

        public ScenarioParameterBuilder value(String value) {
            scenarioParameter.value = value;
            return this;
        }

        public ScenarioParameterBuilder required() {
            scenarioParameter.required = true;
            return this;
        }

        public ScenarioParameterBuilder optional() {
            scenarioParameter.required = false;
            return this;
        }

        public ScenarioParameterBuilder label(String label) {
            scenarioParameter.label = label;
            return this;
        }

        public ScenarioParameterBuilder addOption(String key, String value) {
            scenarioParameter.options.add(new ScenarioParameterOption(key, value));
            return this;
        }
    }
}