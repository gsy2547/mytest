package cn.itcast.bos.web.action.system.realm;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import cn.itcast.bos.dao.system.PermissionDao;
import cn.itcast.bos.dao.system.RoleDao;
import cn.itcast.bos.dao.system.UserDao;
import cn.itcast.bos.domain.system.Permission;
import cn.itcast.bos.domain.system.Role;
import cn.itcast.bos.domain.system.User;
import cn.itcast.bos.service.system.RoleService;
import cn.itcast.bos.service.system.UserService;

public class CustomRealm extends AuthorizingRealm{
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private PermissionDao permissoinDao;

	/**
	 * 设置权限
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//获取当前登陆用户
		User user=(User) principals.getPrimaryPrincipal();
		
		SimpleAuthorizationInfo info=new SimpleAuthorizationInfo(); 
		//url拦截所需要的权限
		//info.addStringPermission("courier");
		//根据用户来查询角色
		List<Role> listRole = user.getUsername().equals("admin") ? roleDao.findAll() : roleDao.findRoleByUser(user.getId());
		for (Role role : listRole) {
			info.addRole(role.getKeyword());
		}
		//通过用户来获得当前所有的权限
		List<Permission>listPermission=user.getUsername().equals("admin") ? permissoinDao.findAll() : permissoinDao.findByUser(user.getId());
		for (Permission permission : listPermission) {
			info.addStringPermission(permission.getKeyword());
		}
		return info;
	}
	
	
	/**
	 * 认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		//第一步把token转成UsernamePasswordToken类型
		UsernamePasswordToken usernamePasswordToken=(UsernamePasswordToken)token;
		//判断账号是否存在，不存在返回null值。
		if (StringUtils.isNotBlank(usernamePasswordToken.getUsername())) {
			//表示登录时输入的有用户名
			//通过用户名去查询密码
			User user = userDao.findByUsername(usernamePasswordToken.getUsername());
			//判断当前用户是否存在
			if (user!=null) {
				//查询到了当前用户
				//principal我们一般返回当前对象，只要在这个地方传入了当前对象，那么在我们项目中任意地方都可以获取到。
				//credentials传入的是数据库中查询出来的密码
				//realmName当前自定义的realm的名称
				AuthenticationInfo info = new SimpleAuthenticationInfo(user, user.getPassword(), this.getName());
				return info;
			} else {
				//表示虽然输入了用户名，但是数据库中没有该用户
				return null;
			}
		}else {
			//表示虽然输入了用户名，但是数据库中没有该用户
			return null;
		}
	}

}
