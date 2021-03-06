package com.x.organization.core.entity;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.jdbc.ContainerTable;
import org.apache.openjpa.persistence.jdbc.ElementColumn;
import org.apache.openjpa.persistence.jdbc.ElementIndex;
import org.apache.openjpa.persistence.jdbc.Index;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.SliceJpaObject;
import com.x.base.core.entity.annotation.CheckPersist;
import com.x.base.core.entity.annotation.CheckRemove;
import com.x.base.core.entity.annotation.CitationExist;
import com.x.base.core.entity.annotation.CitationNotExist;
import com.x.base.core.entity.annotation.ContainerEntity;
import com.x.base.core.entity.annotation.Flag;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.tools.DateTools;

@Entity
@ContainerEntity(dumpSize = 200, type = ContainerEntity.Type.content, reference = ContainerEntity.Reference.strong)
@Table(name = PersistenceProperties.Unit.table, uniqueConstraints = {
		@UniqueConstraint(name = PersistenceProperties.Unit.table + JpaObject.IndexNameMiddle
				+ JpaObject.DefaultUniqueConstraintSuffix, columnNames = { JpaObject.IDCOLUMN,
						JpaObject.CREATETIMECOLUMN, JpaObject.UPDATETIMECOLUMN, JpaObject.SEQUENCECOLUMN }) })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Unit extends SliceJpaObject {

	private static final long serialVersionUID = -4434929262285217462L;

	private static final String TABLE = PersistenceProperties.Unit.table;

	public static final Integer TOP_LEVEL = 1;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@FieldDescribe("???????????????,????????????.")
	@Id
	@Column(length = length_id, name = ColumnNamePrefix + id_FIELDNAME)
	@CheckRemove(citationNotExists = {
			/* ???????????????????????? */
			@CitationNotExist(type = Unit.class, fields = Unit.superior_FIELDNAME),
			/* ???????????????????????? */
			@CitationNotExist(type = Identity.class, fields = Identity.unit_FIELDNAME),
			/* ?????????????????? */
			@CitationNotExist(type = UnitAttribute.class, fields = UnitAttribute.unit_FIELDNAME),
			/* ?????????????????? */
			@CitationNotExist(type = UnitDuty.class, fields = UnitDuty.unit_FIELDNAME) })
	private String id = createId();

	/* ????????? JpaObject ???????????? */

	public void onPersist() throws Exception {
		this.pinyin = StringUtils.lowerCase(PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE));
		this.pinyinInitial = StringUtils.lowerCase(PinyinHelper.getShortPinyin(name));
		this.superior = StringUtils.trimToEmpty(this.superior);
		this.unique = StringUtils.isBlank(this.unique) ? id : unique;
		this.distinguishedName = this.name + PersistenceProperties.distinguishNameSplit + this.unique
				+ PersistenceProperties.distinguishNameSplit + PersistenceProperties.Unit.distinguishNameCharacter;
		/** ????????????????????? */
		if (null == this.orderNumber) {
			this.orderNumber = DateTools.timeOrderNumber();
		}
	}

	/** ?????????????????? */

	public static final String name_FIELDNAME = "name";
	@FieldDescribe("??????,????????????.")
	@Column(length = length_255B, name = ColumnNamePrefix + name_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + name_FIELDNAME)
	@CheckPersist(allowEmpty = false)
	private String name;

	public static final String unique_FIELDNAME = "unique";
	@Flag
	@FieldDescribe("????????????,????????????,??????????????????????????????")
	@Column(length = length_255B, name = ColumnNamePrefix + unique_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + unique_FIELDNAME)
	@CheckPersist(allowEmpty = true, simplyString = true, citationNotExists = @CitationNotExist(fields = {
			"unique" }, type = Unit.class))
	private String unique;

	public static final String distinguishedName_FIELDNAME = "distinguishedName";
	@Flag
	@FieldDescribe("?????????.???@U??????.")
	@Column(length = length_255B, name = ColumnNamePrefix + distinguishedName_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + distinguishedName_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String distinguishedName;

	public static final String typeList_FIELDNAME = "typeList";
	@FieldDescribe("????????????.")
	@PersistentCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ContainerTable(name = TABLE + ContainerTableNameMiddle + typeList_FIELDNAME, joinIndex = @Index(name = TABLE
			+ IndexNameMiddle + typeList_FIELDNAME + JoinIndexNameSuffix))
	@ElementColumn(length = JpaObject.length_255B, name = ColumnNamePrefix + typeList_FIELDNAME)
	@ElementIndex(name = TABLE + IndexNameMiddle + typeList_FIELDNAME + ElementIndexNameSuffix)
	@CheckPersist(allowEmpty = true)
	private List<String> typeList;

	public static final String pinyin_FIELDNAME = "pinyin";
	@FieldDescribe("name??????,????????????")
	@Index(name = TABLE + IndexNameMiddle + pinyin_FIELDNAME)
	@Column(length = length_255B, name = ColumnNamePrefix + pinyin_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String pinyin;

	public static final String pinyinInitial_FIELDNAME = "pinyinInitial";
	@FieldDescribe("name???????????????,????????????")
	@Column(length = length_255B, name = ColumnNamePrefix + pinyinInitial_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + pinyinInitial_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String pinyinInitial;

	public static final String description_FIELDNAME = "description";
	@FieldDescribe("??????.")
	@Column(length = JpaObject.length_255B, name = ColumnNamePrefix + description_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + description_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String description;

	public static final String shortName_FIELDNAME = "shortName";
	@CheckPersist(allowEmpty = true)
	@FieldDescribe("???????????????")
	@Column(length = length_255B, name = ColumnNamePrefix + shortName_FIELDNAME)
	private String shortName;

	public static final String level_FIELDNAME = "level";
	@FieldDescribe("????????????,1??????????????????.????????????.")
	@Column(name = ColumnNamePrefix + level_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + level_FIELDNAME)
	@CheckPersist(allowEmpty = false)
	private Integer level;

	public static final String levelName_FIELDNAME = "levelName";
	@FieldDescribe("??????????????????.")
	@Lob
	@Basic(fetch = FetchType.EAGER)
	@CheckPersist(allowEmpty = false)
	@Column(length = JpaObject.length_2K, name = ColumnNamePrefix + levelName_FIELDNAME)
	private String levelName;

	public static final String levelOrderNumber_FIELDNAME = "levelOrderNumber";
	@FieldDescribe("???????????????,??????" + PersistenceProperties.Unit.levelNameSplit
			+ "????????????.?????????unit??????orderNumber???Integer???????????????Organization?????????????????????????????????,?????????0")
	@CheckPersist(allowEmpty = true)
	@Column(length = JpaObject.length_255B, name = ColumnNamePrefix + levelOrderNumber_FIELDNAME)
	private String levelOrderNumber;

	public static final String superior_FIELDNAME = "superior";
	@FieldDescribe("????????????.")
	@Column(length = JpaObject.length_id, name = ColumnNamePrefix + superior_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + superior_FIELDNAME)
	@CheckPersist(allowEmpty = true, citationExists = { @CitationExist(type = Unit.class) })
	private String superior;

	public static final String orderNumber_FIELDNAME = "orderNumber";
	@FieldDescribe("?????????,????????????,???????????????")
	@Column(name = ColumnNamePrefix + orderNumber_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + orderNumber_FIELDNAME)
	private Integer orderNumber;

	public static final String controllerList_FIELDNAME = "controllerList";
	@FieldDescribe("?????????????????????.")
	@PersistentCollection(fetch = FetchType.EAGER)
	@OrderColumn(name = ORDERCOLUMNCOLUMN)
	@ContainerTable(name = TABLE + ContainerTableNameMiddle + controllerList_FIELDNAME, joinIndex = @Index(name = TABLE
			+ IndexNameMiddle + controllerList_FIELDNAME + JoinIndexNameSuffix))
	@ElementColumn(length = JpaObject.length_id, name = ColumnNamePrefix + controllerList_FIELDNAME)
	@ElementIndex(name = TABLE + controllerList_FIELDNAME + ElementIndexNameSuffix)
	@CheckPersist(allowEmpty = true, citationExists = @CitationExist(type = Person.class))
	private List<String> controllerList;

	public static final String dingdingId_FIELDNAME = "dingdingId";
	@FieldDescribe("????????????ID.")
	@Column(length = length_255B, name = ColumnNamePrefix + dingdingId_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + dingdingId_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String dingdingId;

	public static final String dingdingHash_FIELDNAME = "dingdingHash";
	@FieldDescribe("????????????????????????.")
	@Column(length = length_255B, name = ColumnNamePrefix + dingdingHash_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + dingdingHash_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String dingdingHash;

	public static final String weLinkId_FIELDNAME = "weLinkId";
	@FieldDescribe("WeLink??????ID.")
	@Column(length = length_255B, name = ColumnNamePrefix + weLinkId_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + weLinkId_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String weLinkId;

	public static final String weLinkHash_FIELDNAME = "weLinkHash";
	@FieldDescribe("WeLink??????????????????.")
	@Column(length = length_255B, name = ColumnNamePrefix + weLinkHash_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + weLinkHash_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String weLinkHash;

	public static final String qiyeweixinId_FIELDNAME = "qiyeweixinId";
	@FieldDescribe("??????????????????ID.")
	@Column(length = length_255B, name = ColumnNamePrefix + qiyeweixinId_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + qiyeweixinId_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String qiyeweixinId;

	public static final String zhengwuDingdingId_FIELDNAME = "zhengwuDingdingId";
	@FieldDescribe("??????????????????ID.")
	@Column(length = length_255B, name = ColumnNamePrefix + zhengwuDingdingId_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + zhengwuDingdingId_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String zhengwuDingdingId;

	public static final String zhengwuDingdingHash_FIELDNAME = "zhengwuDingdingHash";
	@FieldDescribe("??????????????????????????????.")
	@Column(length = length_255B, name = ColumnNamePrefix + zhengwuDingdingHash_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + zhengwuDingdingHash_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String zhengwuDingdingHash;

	public static final String qiyeweixinHash_FIELDNAME = "qiyeweixinHash";
	@FieldDescribe("??????????????????????????????.")
	@Column(length = length_255B, name = ColumnNamePrefix + qiyeweixinHash_FIELDNAME)
	@Index(name = TABLE + IndexNameMiddle + qiyeweixinHash_FIELDNAME)
	@CheckPersist(allowEmpty = true)
	private String qiyeweixinHash;

	/* flag????????? */

	// public static String[] FLA GS = new String[] { JpaObject.id_FIELDNAME,
	// unique_FIELDNAME,
	// distinguishedName_FIELDNAME };

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getSuperior() {
		return superior;
	}

	public void setSuperior(String superior) {
		this.superior = superior;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getPinyinInitial() {
		return pinyinInitial;
	}

	public void setPinyinInitial(String pinyinInitial) {
		this.pinyinInitial = pinyinInitial;
	}

	public String getUnique() {
		return unique;
	}

	public void setUnique(String unique) {
		this.unique = unique;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}

	public List<String> getControllerList() {
		return controllerList;
	}

	public void setControllerList(List<String> controllerList) {
		this.controllerList = controllerList;
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<String> typeList) {
		this.typeList = typeList;
	}

	public String getDingdingId() {
		return dingdingId;
	}

	public void setDingdingId(String dingdingId) {
		this.dingdingId = dingdingId;
	}

	public String getDingdingHash() {
		return dingdingHash;
	}

	public void setDingdingHash(String dingdingHash) {
		this.dingdingHash = dingdingHash;
	}

	public String getQiyeweixinId() {
		return qiyeweixinId;
	}

	public void setQiyeweixinId(String qiyeweixinId) {
		this.qiyeweixinId = qiyeweixinId;
	}

	public String getQiyeweixinHash() {
		return qiyeweixinHash;
	}

	public void setQiyeweixinHash(String qiyeweixinHash) {
		this.qiyeweixinHash = qiyeweixinHash;
	}

	public String getZhengwuDingdingId() {
		return zhengwuDingdingId;
	}

	public void setZhengwuDingdingId(String zhengwuDingdingId) {
		this.zhengwuDingdingId = zhengwuDingdingId;
	}

	public String getZhengwuDingdingHash() {
		return zhengwuDingdingHash;
	}

	public void setZhengwuDingdingHash(String zhengwuDingdingHash) {
		this.zhengwuDingdingHash = zhengwuDingdingHash;
	}

	public String getWeLinkId() {
		return weLinkId;
	}

	public void setWeLinkId(String weLinkId) {
		this.weLinkId = weLinkId;
	}

	public String getWeLinkHash() {
		return weLinkHash;
	}

	public void setWeLinkHash(String weLinkHash) {
		this.weLinkHash = weLinkHash;
	}

	public String getLevelOrderNumber() {
		return levelOrderNumber;
	}

	public void setLevelOrderNumber(String levelOrderNumber) {
		this.levelOrderNumber = levelOrderNumber;
	}
}