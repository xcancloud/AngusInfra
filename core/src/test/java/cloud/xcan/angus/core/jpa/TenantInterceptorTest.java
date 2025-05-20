package cloud.xcan.angus.core.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cloud.xcan.angus.core.jpa.interceptor.TenantInterceptor;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

public class TenantInterceptorTest {

  @BeforeClass
  public static void init() {
    Set<String> annoTables = new HashSet<>();
    annoTables.add("user");
    TenantInterceptor.TENANT_TABLES.addAll(annoTables);
  }

  @Test
  public void testSelect() {
    String case11 = "select * from user";
    assertEquals("select * from user WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case11, PrincipalContext.getTenantId()));
    String case12 = "select * from user limit 10";
    assertEquals("select * from user WHERE tenant_id=-1 limit 10",
        TenantInterceptor.getTenantSql(case12, PrincipalContext.getTenantId()));
    String case13 = "SELECT * from user";
    assertEquals(
        "SELECT * from user WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case13, PrincipalContext.getTenantId()));
    String case14 = "SELECT * from user order by id asc";
    assertEquals(
        "SELECT * from user WHERE tenant_id=-1 order by id asc",
        TenantInterceptor.getTenantSql(case14, PrincipalContext.getTenantId()));
    String case21 = "select * FROM user";
    assertEquals(
        "select * FROM user WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case21, PrincipalContext.getTenantId()));
    String case22 = "SELECT * FROM user";
    assertEquals(
        "SELECT * FROM user WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case22, PrincipalContext.getTenantId()));
    String case31 = "select * from user u";
    assertEquals(
        "select * from user u WHERE u.tenant_id=-1",
        TenantInterceptor.getTenantSql(case31, PrincipalContext.getTenantId()));
    String case32 = "select * from user limit 10";
    assertEquals("select * from user WHERE tenant_id=-1 limit 10",
        TenantInterceptor.getTenantSql(case32, PrincipalContext.getTenantId()));
    String case41 = "select * from user u where 1=1";
    assertEquals(
        "select * from user u where u.tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case41, PrincipalContext.getTenantId()));
    String case42 = "select * from user u WHERE 1=1";
    assertEquals(
        "select * from user u WHERE u.tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case42, PrincipalContext.getTenantId()));
    String case43 = "select * from user u WHERE 1=1 order by u.id desc";
    assertEquals(
        "select * from user u WHERE u.tenant_id=-1 AND 1=1 order by u.id desc",
        TenantInterceptor.getTenantSql(case43, PrincipalContext.getTenantId()));
    String case51 = "select * from user where 1=1";
    assertEquals(
        "select * from user where tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case51, PrincipalContext.getTenantId()));
    String case52 = "select * from user WHERE 1=1";
    assertEquals(
        "select * from user WHERE tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case52, PrincipalContext.getTenantId()));
    String case61 = "SELECT * from user u inner join dept d on u.dept_id = d.id where 1=1";
    assertEquals(
        "SELECT * from user u inner join dept d on u.dept_id = d.id where u.tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case61, PrincipalContext.getTenantId()));
    String case62 = "SELECT * from user u inner join dept d on u.dept_id = d.id";
    assertEquals(
        "SELECT * from user u inner join dept d on u.dept_id = d.id WHERE u.tenant_id=-1",
        TenantInterceptor.getTenantSql(case62, PrincipalContext.getTenantId()));
    String case71 = "SELECT * from user u inner join dept d on u.dept_id = d.id order by u.id asc";
    assertEquals(
        "SELECT * from user u inner join dept d on u.dept_id = d.id WHERE u.tenant_id=-1 order by u.id asc",
        TenantInterceptor.getTenantSql(case71, PrincipalContext.getTenantId()));
    String case72 = "SELECT * from user u inner join dept d on u.dept_id = d.id limit 10";
    assertEquals(
        "SELECT * from user u inner join dept d on u.dept_id = d.id WHERE u.tenant_id=-1 limit 10",
        TenantInterceptor.getTenantSql(case72, PrincipalContext.getTenantId()));
    String case73 = "SELECT * from user u inner join dept d on u.dept_id = d.id order by u.id asc limit 10";
    assertEquals(
        "SELECT * from user u inner join dept d on u.dept_id = d.id WHERE u.tenant_id=-1 order by u.id asc limit 10",
        TenantInterceptor.getTenantSql(case73, PrincipalContext.getTenantId()));
  }

  @Test
  public void testDelete() {
    String case1 = "DELETE * from user";
    assertEquals(
        "DELETE * from user WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case1, PrincipalContext.getTenantId()));
    String case2 = "delete * from user";
    assertEquals(
        "delete * from user WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case2, PrincipalContext.getTenantId()));
    String case3 = "delete * from user u";
    assertEquals(
        "delete * from user u WHERE u.tenant_id=-1",
        TenantInterceptor.getTenantSql(case3, PrincipalContext.getTenantId()));
    String case4 = "delete * from user where 1=1";
    assertEquals(
        "delete * from user where tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case4, PrincipalContext.getTenantId()));
    String case5 = "delete * from user u WHERE 1=1";
    assertEquals(
        "delete * from user u WHERE u.tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case5, PrincipalContext.getTenantId()));
  }

  @Test
  public void testUpdate() {
    String case1 = "update user set name='job'";
    assertEquals(
        "update user set name='job' WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case1, PrincipalContext.getTenantId()));
    String case2 = "UPDATE user set name='job'";
    assertEquals(
        "UPDATE user set name='job' WHERE tenant_id=-1",
        TenantInterceptor.getTenantSql(case2, PrincipalContext.getTenantId()));
    String case3 = "UPDATE user set name='job' where 1=1";
    assertEquals(
        "UPDATE user set name='job' where tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case3, PrincipalContext.getTenantId()));
    String case4 = "UPDATE user u set u.name='job'";
    assertEquals(
        "UPDATE user u set u.name='job' WHERE u.tenant_id=-1",
        TenantInterceptor.getTenantSql(case4, PrincipalContext.getTenantId()));
    String case5 = "UPDATE user u set u.name='job' where 1=1";
    assertEquals(
        "UPDATE user u set u.name='job' where u.tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case5, PrincipalContext.getTenantId()));
    String case6 = "UPDATE user u set u.name='job' where 1=1";
    assertEquals(
        "UPDATE user u set u.name='job' where u.tenant_id=-1 AND 1=1",
        TenantInterceptor.getTenantSql(case6, PrincipalContext.getTenantId()));
  }

}
