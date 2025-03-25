/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package cloud.xcan.angus.spec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Mark feature is not complete or needs to continue to be optimized, and can be done in the
 * future.
 * <p>
 * Please use TODO in comments if you need to do it currently.
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR,
    ElementType.METHOD})
public @interface DoInFuture {

  String value();
}
