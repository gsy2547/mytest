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

import cn.itcast.bos.domain.system.Role;
import cn.itcast.bos.service.system.RoleService;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class RoleAction extends ActionSupport implements ModelDriven<Role> {

	private Role model=new Role();
	public Role getModel() {
		return model;
	}
	@Autowired
	private RoleService roleService;
	//角色管理列表role_findAll
	@Action(value="role_findAll")
	public String findAll() throws IOException{
		List<Role>list=roleService.findAll();
		//转为json格式的数据
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String []{"users","permissions","menus"});
		JSONArray json=JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
	
	private String menuIds;
	private Integer [] permissionIds;
	public void setMenuIds(String menuIds) {
		this.menuIds = menuIds;
	}
	public void setPermissionIds(Integer[] permissionIds) {
		this.permissionIds = permissionIds;
	}


	//角色保存role_save
	@Action(value="role_save",results={@Result(name="save",type="redirect", location="/pages/system/role.html")})
	public String save(){
		roleService.save(model, menuIds, permissionIds);
		return "save";
	}
	
}
