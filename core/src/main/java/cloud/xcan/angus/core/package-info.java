/**
 * Core Base Package - Fundamental utilities and business templates.
 *
 * <p>This package contains the core functionality extracted from the monolithic 'core' module:
 * <ul>
 *   <li>BizTemplate - Business logic execution template with tracing</li>
 *   <li>Exception handling framework</li>
 *   <li>Common utilities</li>
 *   <li>Spring integration</li>
 * </ul>
 *
 * <h2>Design Goals</h2>
 * <ul>
 *   <li>Single Responsibility: Only core business logic, no persistence/messaging/etc.</li>
 *   <li>Lightweight: Minimal dependencies (< 10 core libraries)</li>
 *   <li>High Reusability: Required by all Angus applications</li>
 *   <li>Backward Compatible: Maintains original package names and APIs</li>
 * </ul>
 *
 * <h2>Migration from 'core' module</h2>
 * <p>This module is part of the core module refactoring initiative. For complete
 * migration instructions, see CORE_MODULE_REFACTORING_PLAN.md
 *
 * @version 2.0.0
 * @since 2.0.0
 */
package cloud.xcan.angus.core;
