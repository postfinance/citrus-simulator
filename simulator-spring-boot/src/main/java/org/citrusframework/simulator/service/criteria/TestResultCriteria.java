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
 * Criteria class for the {@link org.citrusframework.simulator.model.TestResult} entity. This class is used
 * in {@link org.citrusframework.simulator.web.rest.TestResultResource} to receive all the possible filtering options
 * from the Http GET request parameters.
 * <p>
 * For example the following could be a valid request:
 * {@code /test-results?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * <p>
 * As Spring is unable to properly convert the types, unless
 * specific {@link org.citrusframework.simulator.service.filter.Filter} class are used, we need to use fix type specific
 * filters.
 */
@Getter
@Setter
@ToString
@ParameterObject
public class TestResultCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private @Nullable LongFilter id;

    private @Nullable IntegerFilter status;

    private @Nullable StringFilter testName;

    private @Nullable StringFilter className;

    private @Nullable StringFilter errorMessage;

    private @Nullable StringFilter stackTrace;

    private @Nullable StringFilter failureType;

    private @Nullable InstantFilter createdDate;

    private @Nullable InstantFilter lastModifiedDate;

    private @Nullable StringFilter testParameterKey;

    private @Nullable Boolean distinct;

    public TestResultCriteria() {
    }

    public TestResultCriteria(TestResultCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.testName = other.testName == null ? null : other.testName.copy();
        this.className = other.className == null ? null : other.className.copy();
        this.errorMessage = other.errorMessage == null ? null : other.errorMessage.copy();
        this.stackTrace = other.stackTrace == null ? null : other.stackTrace.copy();
        this.failureType = other.failureType == null ? null : other.failureType.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.testParameterKey = other.testParameterKey == null ? null : other.testParameterKey.copy();
        this.distinct = other.distinct;
    }

    @Override
    public TestResultCriteria copy() {
        return new TestResultCriteria(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TestResultCriteria testResultCriteria)) {
            return false;
        }

        return new EqualsBuilder()
            .append(id, testResultCriteria.id)
            .append(status, testResultCriteria.status)
            .append(testName, testResultCriteria.testName)
            .append(className, testResultCriteria.className)
            .append(errorMessage, testResultCriteria.errorMessage)
            .append(stackTrace, testResultCriteria.stackTrace)
            .append(failureType, testResultCriteria.failureType)
            .append(createdDate, testResultCriteria.createdDate)
            .append(lastModifiedDate, testResultCriteria.lastModifiedDate)
            .append(testParameterKey, testResultCriteria.testParameterKey)
            .append(distinct, testResultCriteria.distinct)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(status)
            .append(testName)
            .append(className)
            .append(errorMessage)
            .append(stackTrace)
            .append(failureType)
            .append(createdDate)
            .append(lastModifiedDate)
            .append(testParameterKey)
            .append(distinct)
            .toHashCode();
    }
}
