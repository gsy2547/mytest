package cn.itcast.bos.web.action.system;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.system.Menu;
import cn.itcast.bos.domain.system.User;
import cn.itcast.bos.service.system.MenuService;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class MenuAction extends ActionSupport implements ModelDriven<Menu> {
	
	private Menu model=new Menu();
	public Menu getModel() {
		
		return model;
	}
	@Autowired
	private MenuService menuService;
	
	
	//查询menu_findAll
	@Action(value="menu_findAll")
	public String findAll() throws IOException{
		
		List<Menu>list=menuService.findAll();
		//转为json格式的数据
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String []{"roles","childrenMenus","parentMenu"});
		JSONArray json=JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
	//菜单管理保存
	@Action(value="menu_save", results={@Result(name="save", type="redirect", location="/pages/system/menu.html")})
	public String save(){
		menuService.save(model);
		return "save";
	}
	
	//五、 系统菜单根据登录人动态展示menu_findMenuByUser
	@Action(value="menu_findMenuByUser")
	public String findMenuByUser() throws IOException{
		//首先要获得登陆的用户名
		User user=(User) ServletActionContext.getRequest().getSession().getAttribute("loginUser");
		//在根据用户名查询所要显示的的菜单
		List<Menu>list=menuService.findMenuByUser(user);
		//转为json格式的数据
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String []{"roles", "childrenMenus", "children", "parentMenu"});
		JSONArray json=JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
	
}
