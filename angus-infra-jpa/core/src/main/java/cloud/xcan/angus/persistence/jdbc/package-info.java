/**
 * Persistence JDBC Package - Spring Data JDBC integration.
 *
 * <p>This package contains JDBC persistence functionality extracted from the 'core' module:
 * <ul>
 *   <li>JDBC data access patterns</li>
 *   <li>Multi-tenant data isolation</li>
 *   <li>Audit logging</li>
 *   <li>Transaction management</li>
 * </ul>
 *
 * <h2>Design Goals</h2>
 * <ul>
 *   <li>Single Responsibility: JDBC persistence only</li>
 *   <li>Optional: Applications can exclude if not using JDBC</li>
 *   <li>Spring Data Integration: Built on Spring Data JDBC</li>
 *   <li>Backward Compatible: Maintains original package names</li>
 * </ul>
 *
 * <h2>Migration from 'core' module</h2>
 * <p>Content migrated from: core/src/main/java/cloud/xcan/angus/core/jdbc/
 *
 * @version 2.0.0
 * @since 2.0.0
 */
package cloud.xcan.angus.persistence.jdbc;
