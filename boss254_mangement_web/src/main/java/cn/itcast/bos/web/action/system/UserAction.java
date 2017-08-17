package cn.itcast.bos.web.action.system;


import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
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

import aj.org.objectweb.asm.Type;
import cn.itcast.bos.domain.system.User;
import cn.itcast.bos.service.system.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
@Controller
@Scope("prototype")
@ParentPackage("struts-default")
@Namespace("/")
public class UserAction extends ActionSupport implements ModelDriven<User> {

	private User model=new User();
	public User getModel() {
		return model;
	}
	
	@Autowired
	private UserService userService;
	
	
	
	private String checkcode;
	public void setCheckcode(String checkcode) {
		this.checkcode = checkcode;
	}
	//用shiro做登陆功能
	@Action(value="user_login", results={@Result(name="success", type="redirect", location="/index.html"),
			@Result(name="input", type="redirect", location="/login.html")})
	public String login(){
		//用户输入的验证码是否正确
		String checkcodeSession=(String) ServletActionContext.getRequest().getSession().getAttribute("key");
		if (StringUtils.isNotBlank(checkcode)&&checkcode.equals(checkcodeSession)) {
			//获得当前用户subject接口
			Subject subject=SecurityUtils.getSubject();
			//AuthenticationToken来为subject的登录方法封装参数。
			AuthenticationToken token=new UsernamePasswordToken(model.getUsername(),model.getPassword());
			try {
				//调用subject的登陆方法
				subject.login(token);
				//获取当前登录的用户
				User user = (User) subject.getPrincipal();
				//将当前对象放入session中
				ServletActionContext.getRequest().getSession().setAttribute("loginUser", user);
				return SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return INPUT;
		}
		return INPUT;
	}
	//退出系统user_logout
	@Action(value="user_logout",results={@Result(name="logout",type="redirect", location="/login.html")})
	public String logout(){
		//基于shiro完成退出
		Subject subject=SecurityUtils.getSubject();
		subject.logout();
		
		return "logout";
	}
	//系统管理用户管理列表user_findAll
	@Action(value="user_findAll")
	public String findAll() throws IOException{
		List<User>list=userService.findAll();
		// 转为json格式
		JsonConfig jsonConfig=new JsonConfig();
		jsonConfig.setExcludes(new String[]{"roles"});
		JSONArray json=JSONArray.fromObject(list, jsonConfig);
		//解决乱码问题
		ServletActionContext.getResponse().setContentType("text/json;charset=utf-8");
		ServletActionContext.getResponse().getWriter().print(json);
		return NONE;
	}
	
	private Integer[] roleIds;
	public void setRoleIds(Integer[] roleIds) {
		this.roleIds = roleIds;
	}
	@Action(value="user_save", results={@Result(name="success", type="redirect", location="/pages/system/userlist.html")})
	public String save(){
		userService.save(model, roleIds);
		return SUCCESS;
	}
	
}
