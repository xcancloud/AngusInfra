package cloud.xcan.angus.core.jpa.interceptor;

import static cloud.xcan.angus.core.utils.CoreUtils.getAnnotationClasses;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.decideMultiTenantCtrlByApiType;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isMultiTenantCtrl;
import static cloud.xcan.angus.spec.experimental.BizConstant.TENANT_ID_DB_KEY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.experimental.MultiTenant;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.util.CollectionUtils;

/**
 * The alias `as` keyword is not supported.
 *
 * @author XiaoLong Liu
 */
@Slf4j
@Data
@Accessors(chain = true)
public class TenantInterceptor implements StatementInspector {

  public static Set<String> TENANT_TABLES = new CopyOnWriteArraySet<>();

  public TenantInterceptor() {
    if (CollectionUtils.isEmpty(TENANT_TABLES)) {
      TENANT_TABLES = loadAnnotationTable("cloud.xcan.angus", MultiTenant.class);
    }
  }

  @Override
  public String inspect(String sql) {
    Principal principal = PrincipalContext.get();
    try {
      if (!isMultiTenantCtrl(principal) || !decideMultiTenantCtrlByApiType(principal)
          || isEmpty(TENANT_TABLES)) {
        return sql;
      }
      // Fix:: The operation administrator operates himself tenant
      //      Long opTenantId = getRealOptTenantId(principal);
      //      if (isTopUser() && isNull(opTenantId)) {
      //        return sql;
      //      }

      if (log.isDebugEnabled()) {
        log.debug("Parse the original sql：{}", sql);
      }
      String newSql = getTenantSql(sql, getOptTenantId(principal));
      if (log.isDebugEnabled()) {
        log.debug("Parse the tenant sql result：{}", newSql);
      }
      return newSql;
    } catch (Exception e) {
      log.error("Parse the tenant sql exception：{}", e.getMessage(), e);
    }
    return null;
  }

  public static String getTenantSql(String sql, Long tenantId) {
    char[] sqlChars = sql.toCharArray();
    StringBuilder tableName = new StringBuilder();
    if (sql.startsWith("select") || sql.startsWith("delete")
        || sql.startsWith("SELECT") || sql.startsWith("DELETE")) {
      // find table name
      int index = sql.indexOf("FROM");
      index = index > 0 ? index : sql.indexOf("from");
      int tableEndIndex = 0;
      boolean passStart = false;
      for (int i = index + 5; i < sqlChars.length; i++) {
        if (sqlChars[i] == ' ' && !passStart) {
          continue;
        } else {
          passStart = true;
        }
        if (sqlChars[i] != ' ') {
          tableName.append(sqlChars[i]);
          continue;
        }
        tableEndIndex = i;
        break;
      }
      if (!TENANT_TABLES.contains(tableName.toString())) {
        return sql;
      }
      StringBuilder newSql = new StringBuilder();
      if (tableEndIndex <= 0) { // none alias and where
        newSql.append(sql).append(" WHERE ").append(TENANT_ID_DB_KEY).append("=").append(tenantId);
        return newSql.toString();
      }
      // find table alias
      StringBuilder tableAlias = new StringBuilder();
      int aliasEndIndex = getWhereEndIndex(sqlChars, tableEndIndex, tableAlias);
      String alis = tableAlias.toString();
      if (aliasEndIndex <= 0) { // none where
        newSql.append(sql)
            .append(" WHERE ")
            .append(alis).append(".").append(TENANT_ID_DB_KEY).append("=")
            .append(tenantId);
        return newSql.toString();
      }
      if (StringUtils.isBlank(alis)) { // none alias
        newSql.append(sql).append(" ").append(TENANT_ID_DB_KEY).append("=").append(tenantId);
        return newSql.toString();
      }
      if ("where".equalsIgnoreCase(alis)) {// none alias
        newSql.append(sql.substring(0, aliasEndIndex + 1))
            .append(TENANT_ID_DB_KEY).append("=").append(tenantId)
            .append(" AND ").append(sql.substring(aliasEndIndex + 1));
        return newSql.toString();
      } else if ("order".equalsIgnoreCase(alis) || "limit".equalsIgnoreCase(alis)) {
        newSql.append(sql.substring(0, aliasEndIndex - 6)).append(" ").append("WHERE ")
            .append(TENANT_ID_DB_KEY).append("=").append(tenantId)
            .append(sql.substring(aliasEndIndex - 6));
        return newSql.toString();
      } else {
        StringBuilder where = new StringBuilder();
        int whereEndIndex = getWhereEndIndex(sqlChars, aliasEndIndex, where);
        String whereStr = where.toString();
        if ("WHERE".equalsIgnoreCase(whereStr)) { // exist where
          newSql.append(sql.substring(0, whereEndIndex))
              .append(" ")
              .append(alis).append(".").append(TENANT_ID_DB_KEY).append("=")
              .append(tenantId)
              .append(" AND ")
              .append(sql.substring(whereEndIndex + 1));
          return newSql.toString();
        } else if ("left".equalsIgnoreCase(whereStr) || "inner".equalsIgnoreCase(whereStr)
            || "right".equalsIgnoreCase(whereStr) || "join".equalsIgnoreCase(whereStr)) {
          // exist both alias and join
          // continue search where where exist join
          int whereIndex = sql.indexOf("WHERE");
          whereIndex = whereIndex > 0 ? whereIndex : sql.indexOf("where");
          if (whereIndex <= 0) { // none where
            int orderIndex = sql.indexOf("ORDER");
            orderIndex = orderIndex > 0 ? orderIndex : sql.indexOf("order");
            if (orderIndex <= 0) {
              int limitIndex = sql.indexOf("LIMIT");
              limitIndex = limitIndex > 0 ? orderIndex : sql.indexOf("limit");
              if (limitIndex <= 0) {
                newSql.append(sql).append(" ").append("WHERE ").append(alis).append(".")
                    .append(TENANT_ID_DB_KEY).append("=").append(tenantId);
                return newSql.toString();
              } else {
                newSql.append(sql, 0, limitIndex).append("WHERE ").append(alis)
                    .append(".")
                    .append(TENANT_ID_DB_KEY).append("=").append(tenantId)
                    .append(sql.substring(limitIndex - 1));
                return newSql.toString();
              }
            } else {
              newSql.append(sql, 0, orderIndex).append("WHERE ").append(alis)
                  .append(".")
                  .append(TENANT_ID_DB_KEY).append("=").append(tenantId)
                  .append(sql.substring(orderIndex - 1));
              return newSql.toString();
            }
          } else {
            newSql.append(sql, 0, whereIndex + 6).append(alis)
                .append(".")
                .append(TENANT_ID_DB_KEY).append("=").append(tenantId).append(" AND ")
                .append(sql.substring(whereIndex + 6));
            return newSql.toString();
          }
        }
      }
    } else if (sql.startsWith("UPDATE") || sql.startsWith("update")) {
      // find table name
      int index = sql.indexOf("UPDATE");
      index = index > 0 ? index : sql.indexOf("update");
      int tableEndIdex = 0;
      boolean passStart = false;
      for (int i = index + 7; i < sqlChars.length; i++) {
        if (sqlChars[i] == ' ' && !passStart) {
          continue;
        } else {
          passStart = true;
        }
        if (sqlChars[i] != ' ') {
          tableName.append(sqlChars[i]);
          continue;
        }
        tableEndIdex = i;
        break;
      }
      if (!TENANT_TABLES.contains(tableName.toString())) {
        return sql;
      }
      StringBuilder newSql = new StringBuilder();
      if (tableEndIdex <= 0) { // none alias and where
        newSql.append(sql).append(" WHERE ").append(TENANT_ID_DB_KEY).append("=").append(tenantId);
        return newSql.toString();
      }
      // find table alias
      StringBuilder tableAlias = new StringBuilder();
      passStart = false;
      for (int i = tableEndIdex; i < sqlChars.length; i++) {
        if (sqlChars[i] == ' ' && !passStart) {
          continue;
        } else {
          passStart = true;
        }
        if (sqlChars[i] != ' ') {
          tableAlias.append(sqlChars[i]);
        } else {
          break;
        }
      }
      String alis = tableAlias.toString();
      if ("SET".equalsIgnoreCase(alis)) {// none alias
        if (sql.contains("WHERE") || sql.contains("where")) {
          int whereIndex = sql.indexOf("WHERE");
          whereIndex = whereIndex > 0 ? whereIndex : sql.indexOf("where");
          newSql.append(sql.substring(0, whereIndex + 6)).append(TENANT_ID_DB_KEY).append("=")
              .append(tenantId).append(" AND").append(sql.substring(whereIndex + 5));
          return newSql.toString();
        } else { // none where
          newSql.append(sql).append(" WHERE ").append(TENANT_ID_DB_KEY).append("=")
              .append(tenantId);
          return newSql.toString();
        }
      } else { // exist alias
        if (sql.contains("WHERE") || sql.contains("where")) {
          int whereIndex = sql.indexOf("WHERE");
          whereIndex = whereIndex > 0 ? whereIndex : sql.indexOf("where");
          newSql.append(sql.substring(0, whereIndex + 6)).append(alis).append(".")
              .append(TENANT_ID_DB_KEY).append("=")
              .append(tenantId).append(" AND").append(sql.substring(whereIndex + 5));
          return newSql.toString();
        } else { // none where
          newSql.append(sql).append(" WHERE ").append(alis).append(".").append(TENANT_ID_DB_KEY)
              .append("=")
              .append(tenantId);
          return newSql.toString();
        }
      }
    }
    return sql;
  }

  private static int getWhereEndIndex(char[] sqlChars, int aliasEndIndex, StringBuilder where) {
    boolean passStart;
    passStart = false;
    int whereEndIndex = 0;
    for (int i = aliasEndIndex; i < sqlChars.length; i++) {
      if (sqlChars[i] == ' ' && !passStart) {
        continue;
      } else {
        passStart = true;
      }
      if (sqlChars[i] != ' ') {
        where.append(sqlChars[i]);
        continue;
      }
      whereEndIndex = i;
      break;
    }
    return whereEndIndex;
  }

  public static Set<String> loadAnnotationTable(String packageName,
      Class<? extends Annotation> annotation) {
    Set<Class<?>> allClazz = getAnnotationClasses(packageName, annotation);
    Set<String> names = new HashSet<>();
    for (Class c : allClazz) {
      Annotation a = c.getAnnotation(jakarta.persistence.Table.class);
      if (Objects.nonNull(a)) {
        jakarta.persistence.Table t = (jakarta.persistence.Table) a;
        names.add(t.name());
      } else {
        names.add(c.getSimpleName().toLowerCase());
      }
    }
    return names;
  }

}
