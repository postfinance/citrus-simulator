/*
 * Copyright the original author or authors.
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

package org.citrusframework.simulator.service.criteria;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.citrusframework.simulator.service.filter.InstantFilter;
import org.citrusframework.simulator.service.filter.IntegerFilter;
import org.citrusframework.simulator.service.filter.LongFilter;
import org.citrusframework.simulator.service.filter.StringFilter;
import org.springdoc.core.annotations.ParameterObject;

import java.io.Serial;
import java.io.Serializable;

/**
 * Criteria class for the {@link org.citrusframework.simulator.model.ScenarioParameter} entity. This class is used
 * in {@link org.citrusframework.simulator.web.rest.ScenarioParameterResource} to receive all the possible filtering
 * options from the Http GET request parameters.
 * <p>
 * For example the following could be a valid request:
 * {@code /scenario-parameters?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * <p>
 * As Spring is unable to properly convert the types, unless
 * specific {@link org.citrusframework.simulator.service.filter.Filter} class are used, we need to use fix type
 * specific filters.
 */
@Getter
@Setter
@ToString
@ParameterObject
public class ScenarioParameterCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private @Nullable LongFilter parameterId;

    private @Nullable StringFilter name;

    private @Nullable IntegerFilter controlType;

    private @Nullable StringFilter value;

    private @Nullable InstantFilter createdDate;

    private @Nullable InstantFilter lastModifiedDate;

    private @Nullable LongFilter scenarioExecutionId;

    private @Nullable Boolean distinct;

    public ScenarioParameterCriteria() {
    }

    public ScenarioParameterCriteria(ScenarioParameterCriteria other) {
        this.parameterId = other.parameterId == null ? null : other.parameterId.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.controlType = other.controlType == null ? null : other.controlType.copy();
        this.value = other.value == null ? null : other.value.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.scenarioExecutionId = other.scenarioExecutionId == null ? null : other.scenarioExecutionId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ScenarioParameterCriteria copy() {
        return new ScenarioParameterCriteria(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScenarioParameterCriteria scenarioParameterCriteria)) {
            return false;
        }

        return new EqualsBuilder()
            .append(parameterId, scenarioParameterCriteria.parameterId)
            .append(name, scenarioParameterCriteria.name)
            .append(controlType, scenarioParameterCriteria.controlType)
            .append(value, scenarioParameterCriteria.value)
            .append(createdDate, scenarioParameterCriteria.createdDate)
            .append(lastModifiedDate, scenarioParameterCriteria.lastModifiedDate)
            .append(scenarioExecutionId, scenarioParameterCriteria.scenarioExecutionId)
            .append(distinct, scenarioParameterCriteria.distinct)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(parameterId)
            .append(name)
            .append(controlType)
            .append(value)
            .append(createdDate)
            .append(lastModifiedDate)
            .append(scenarioExecutionId)
            .append(distinct)
            .toHashCode();
    }
}
