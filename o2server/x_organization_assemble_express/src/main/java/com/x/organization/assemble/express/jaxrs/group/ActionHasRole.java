package com.x.organization.assemble.express.jaxrs.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.cache.Cache.CacheKey;
import com.x.base.core.project.cache.CacheManager;
import com.x.base.core.project.gson.GsonPropertyObject;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WrapBoolean;
import com.x.base.core.project.tools.ListTools;
import com.x.organization.assemble.express.Business;
import com.x.organization.core.entity.Group;
import com.x.organization.core.entity.Role;
import com.x.organization.core.entity.Role_;

class ActionHasRole extends BaseAction {

	ActionResult<Wo> execute(EffectivePerson effectivePerson, JsonElement jsonElement) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Wi wi = this.convertToWrapIn(jsonElement, Wi.class);
			ActionResult<Wo> result = new ActionResult<>();
			Business business = new Business(emc);
			CacheKey cacheKey = new CacheKey(this.getClass(), wi.getGroup(), wi.getRoleList());
			Optional<?> optional = CacheManager.get(cacheCategory, cacheKey);
			if (optional.isPresent()) {
				result.setData((Wo) optional.get());
			} else {
				Wo wo = this.get(business, wi);
				CacheManager.put(cacheCategory, cacheKey, wo);
				result.setData(wo);
			}
			return result;
		}
	}

	public static class Wi extends GsonPropertyObject {

		@FieldDescribe("??????")
		private String group;

		@FieldDescribe("??????")
		private List<String> roleList = new ArrayList<>();

		public List<String> getRoleList() {
			return roleList;
		}

		public void setRoleList(List<String> roleList) {
			this.roleList = roleList;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

	}

	public static class Wo extends WrapBoolean {

	}

	private Wo get(Business business, Wi wi) throws Exception {
		Wo wo = new Wo();
		wo.setValue(false);
		if (StringUtils.isEmpty(wi.getGroup()) || ListTools.isEmpty(wi.getRoleList())) {
			return wo;
		}
		Group group = business.group().pick(wi.getGroup());
		if (null == group) {
			return wo;
		}
		List<Role> roles = business.role().pick(wi.getRoleList());
		if (ListTools.isEmpty(roles)) {
			return wo;
		}
		List<String> groupIds = new ArrayList<>();
		groupIds.add(group.getId());
		groupIds.addAll(business.group().listSupNested(group.getId()));
		groupIds = ListTools.trim(groupIds, true, true);
		EntityManager em = business.entityManagerContainer().get(Role.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Role> root = cq.from(Role.class);
		Predicate p = root.get(Role_.groupList).in(groupIds);
		List<String> os = em.createQuery(cq.select(root.get(Role_.id)).where(p))
				.getResultList().stream().distinct().collect(Collectors.toList());
		boolean value = ListTools.containsAny(os,
				ListTools.extractProperty(roles, JpaObject.id_FIELDNAME, String.class, true, true));
		wo.setValue(value);
		return wo;
	}

}