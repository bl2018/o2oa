package com.x.program.center;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.x.base.core.project.cache.CacheManager;
import org.apache.commons.lang3.BooleanUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.program.center.schedule.Area;
import com.x.program.center.schedule.Cleanup;
import com.x.program.center.schedule.CleanupCode;
import com.x.program.center.schedule.CollectLog;
import com.x.program.center.schedule.CollectMarket;
import com.x.program.center.schedule.CollectPerson;
import com.x.program.center.schedule.DingdingSyncOrganization;
import com.x.program.center.schedule.DingdingSyncOrganizationTrigger;
import com.x.program.center.schedule.FireSchedule;
import com.x.program.center.schedule.QiyeweixinSyncOrganization;
import com.x.program.center.schedule.QiyeweixinSyncOrganizationTrigger;
import com.x.program.center.schedule.RefreshApplications;
import com.x.program.center.schedule.TriggerAgent;
import com.x.program.center.schedule.WeLinkSyncOrganization;
import com.x.program.center.schedule.WeLinkSyncOrganizationTrigger;
import com.x.program.center.schedule.ZhengwuDingdingSyncOrganization;
import com.x.program.center.schedule.ZhengwuDingdingSyncOrganizationTrigger;

public class ThisApplication {

	private ThisApplication() {
		// nothing
	}

	protected static Context context;

	public static Context context() {
		return context;
	}

	public static final CenterQueue centerQueue = new CenterQueue();

	public static final LogQueue logQueue = new LogQueue();

	public static final List<Object> dingdingSyncOrganizationCallbackRequest = new ArrayList<>();

	public static final List<Object> weLinkSyncOrganizationCallbackRequest = new ArrayList<>();

	public static final List<Object> zhengwuDingdingSyncOrganizationCallbackRequest = new ArrayList<>();

	public static final List<Object> qiyeweixinSyncOrganizationCallbackRequest = new ArrayList<>();

	public static void init() {
		try {
			CacheManager.init(context.clazz().getSimpleName());
			LoggerFactory.setLevel(Config.logLevel().x_program_center());
			/* 20190927??????????????? */
			context().startQueue(centerQueue);
			context().startQueue(logQueue);

			/* ???????????????????????? */
			if (BooleanUtils.isTrue(Config.zhengwuDingding().getEnable())) {
				/* ?????????????????? */
				context().scheduleLocal(ZhengwuDingdingSyncOrganization.class, Config.zhengwuDingding().getSyncCron());
				/* ?????????????????????????????? */
				context().scheduleLocal(ZhengwuDingdingSyncOrganizationTrigger.class,
						Config.zhengwuDingding().getForceSyncCron());
			}
			/* ???????????????????????? */
			if (BooleanUtils.isTrue(Config.qiyeweixin().getEnable())) {
				/* ?????????????????? */
				context().scheduleLocal(QiyeweixinSyncOrganization.class, Config.qiyeweixin().getSyncCron());
				/* ?????????????????????????????? */
				context().scheduleLocal(QiyeweixinSyncOrganizationTrigger.class,
						Config.qiyeweixin().getForceSyncCron());
			}
			/* ???????????? */
			if (BooleanUtils.isTrue(Config.dingding().getEnable())) {
				/* ?????????????????? */
				context().scheduleLocal(DingdingSyncOrganization.class, Config.dingding().getSyncCron());
				/* ?????????????????????????????? */
				context().scheduleLocal(DingdingSyncOrganizationTrigger.class, Config.dingding().getForceSyncCron());
			}
			/* WeLink?????? */
			if (BooleanUtils.isTrue(Config.weLink().getEnable())) {
				/* ?????????????????? */
				context().scheduleLocal(WeLinkSyncOrganization.class, Config.weLink().getSyncCron());
				/* ?????????????????????????????? */
				context().scheduleLocal(WeLinkSyncOrganizationTrigger.class, Config.weLink().getForceSyncCron());
			}

			context().scheduleLocal(RefreshApplications.class, CenterQueue.REFRESHAPPLICATIONSINTERVAL,
					CenterQueue.REFRESHAPPLICATIONSINTERVAL);
			// ???????????????300????????????120???
			context().scheduleLocal(FireSchedule.class, 180, 120);
			context().scheduleLocal(CleanupCode.class, 10, 60 * 30);
			context().scheduleLocal(Cleanup.class, 10, 60 * 30);
			context().scheduleLocal(CollectPerson.class, 10, 60 * 30);
			context().scheduleLocal(CollectMarket.class, 10, 60 * 60 * 10);
			context().scheduleLocal(CollectLog.class, 10, 60 * 30);
			// ???????????????60????????????30???
			context().scheduleLocal(TriggerAgent.class, 150, 30);
			/* ?????????????????????????????? */
			context().scheduleLocal(Area.class, 300, 60 * 60 * 24 * 7);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void destroy() {
		try {
			CacheManager.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}