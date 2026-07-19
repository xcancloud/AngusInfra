package cloud.xcan.angus.core.biz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.remote.MessageJoinField;
import cloud.xcan.angus.spec.locale.SdfLocaleHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class I18nMessageAspectNestedTest {

  private I18nMessageResolver messageResolver;
  private I18nMessageAspect aspect;
  private Locale previousLocale;

  @BeforeEach
  void setUp() {
    previousLocale = SdfLocaleHolder.getLocale();
    // Use language tag "en" (SupportedLanguage.en); Locale.US ("en_US") is not a SupportedLanguage value
    SdfLocaleHolder.setLocale(Locale.ENGLISH);

    messageResolver = mock(I18nMessageResolver.class);
    I18nMessageProperties properties = new I18nMessageProperties();
    properties.setEnabled(true);
    properties.setDefaultLocale("zh_CN");
    properties.setSkipDefaultLocale(true);

    aspect = new I18nMessageAspect(messageResolver, properties);
  }

  @AfterEach
  void tearDown() {
    if (previousLocale != null) {
      SdfLocaleHolder.setLocale(previousLocale);
    } else {
      SdfLocaleHolder.resetLocaleContext();
    }
  }

  @Test
  void joinArrayVoName_fillsHeterogeneousNestedAndTreeChildren() throws Exception {
    when(messageResolver.resolveBatch(eq("APPLICATION"), any(Set.class), any(Locale.class),
        anyBoolean()))
        .thenReturn(Map.of("GM", "Governance Manager"));
    when(messageResolver.resolveBatch(eq("MENU"), any(Set.class), any(Locale.class), anyBoolean()))
        .thenReturn(Map.of(
            "USER", "Users",
            "USER_LIST", "User List"));

    ParentVo parent = new ParentVo();
    parent.setAccessApp(app("GM", "治理管理者"));

    MenuVo root = menu("USER", "用户");
    MenuVo child = menu("USER_LIST", "用户列表");
    root.setChildren(new ArrayList<>(List.of(child)));
    parent.setMenus(new ArrayList<>(List.of(root)));
    parent.setAuthApps(new ArrayList<>(List.of(app("GM", "治理管理者"))));

    aspect.joinArrayVoName(new Object[]{parent});

    assertEquals("Governance Manager", parent.getAccessApp().getName());
    assertEquals("Governance Manager", parent.getAuthApps().get(0).getName());
    assertEquals("Users", parent.getMenus().get(0).getName());
    assertEquals("User List", parent.getMenus().get(0).getChildren().get(0).getName());
  }

  private static AppVo app(String code, String name) {
    AppVo vo = new AppVo();
    vo.setCode(code);
    vo.setName(name);
    return vo;
  }

  private static MenuVo menu(String code, String name) {
    MenuVo vo = new MenuVo();
    vo.setCode(code);
    vo.setName(name);
    return vo;
  }

  @Data
  static class ParentVo {
    private AppVo accessApp;
    private List<MenuVo> menus;
    private List<AppVo> authApps;
  }

  @Data
  static class AppVo {
    private String code;
    @MessageJoinField(type = "APPLICATION", keyField = "code")
    private String name;
  }

  @Data
  static class MenuVo {
    private String code;
    @MessageJoinField(type = "MENU", keyField = "code")
    private String name;
    private List<MenuVo> children;
  }
}
