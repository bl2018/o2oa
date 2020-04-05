package com.x.attendance.assemble.control.factory;

import com.x.attendance.assemble.control.AbstractFactory;
import com.x.attendance.assemble.control.Business;
import com.x.attendance.assemble.control.exception.DingdingFindNoArgumentError;
import com.x.attendance.assemble.control.exception.QywxFindNoArgumentError;
import com.x.attendance.entity.*;
import com.x.base.core.project.tools.DateTools;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DingdingAttendanceFactory extends AbstractFactory {

    public DingdingAttendanceFactory(Business business) throws Exception {
        super(business);
    }


    /**
     * 查询所有同步记录
     *
     * @param type DingdingQywxSyncRecord.syncType_dingding DingdingQywxSyncRecord.syncType_qywx
     * @return
     * @throws Exception
     */
    public List<DingdingQywxSyncRecord> findAllSyncRecordWithType(String type) throws Exception {
        EntityManager em = this.entityManagerContainer().get(DingdingQywxSyncRecord.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DingdingQywxSyncRecord> query = cb.createQuery(DingdingQywxSyncRecord.class);
        Root<DingdingQywxSyncRecord> root = query.from(DingdingQywxSyncRecord.class);
        Predicate p = cb.equal(root.get(DingdingQywxSyncRecord_.type), type);
        query.select(root).where(p).orderBy(cb.desc(root.get(DingdingQywxSyncRecord_.startTime)));
        return em.createQuery(query).getResultList();
    }

    /**
     * 查询冲突的钉钉同步记录
     *
     * @param fromTime
     * @param toTime
     * @return
     * @throws Exception
     */
    public List<DingdingQywxSyncRecord> findConflictSyncRecord(long fromTime, long toTime) throws Exception {
        EntityManager em = this.entityManagerContainer().get(DingdingQywxSyncRecord.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DingdingQywxSyncRecord> query = cb.createQuery(DingdingQywxSyncRecord.class);
        Root<DingdingQywxSyncRecord> root = query.from(DingdingQywxSyncRecord.class);
        Predicate p = cb.equal(root.get(DingdingQywxSyncRecord_.type), DingdingQywxSyncRecord.syncType_dingding);
        Predicate p1 = cb.or(cb.between(root.get(DingdingQywxSyncRecord_.dateFrom), fromTime, toTime), cb.between(root.get(DingdingQywxSyncRecord_.dateTo), fromTime, toTime));
        p = cb.and(p, p1);
        query.select(root).where(p);
        return em.createQuery(query).getResultList();
    }

    /**
     * 根据用户查询一段时间内的打开数据
     *
     * @param startTime
     * @param endTime
     * @param userId
     * @return
     * @throws Exception
     */
    public List<AttendanceDingtalkDetail> findAllDingdingAttendanceDetail(Date startTime, Date endTime, String userId) throws Exception {
        if (startTime == null && endTime == null && userId == null) {
            throw new DingdingFindNoArgumentError();
        }
        EntityManager em = this.entityManagerContainer().get(AttendanceDingtalkDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AttendanceDingtalkDetail> query = cb.createQuery(AttendanceDingtalkDetail.class);
        Root<AttendanceDingtalkDetail> root = query.from(AttendanceDingtalkDetail.class);
        Predicate p = null;
        if (startTime != null && endTime != null) {
            long start = startTime.getTime();
            long end = endTime.getTime();
            p = cb.between(root.get(AttendanceDingtalkDetail_.userCheckTime), start, end);
        }
        if (userId != null && !userId.isEmpty()) {
            if (p != null) {
                p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.userId), userId));
            } else {
                p = cb.equal(root.get(AttendanceDingtalkDetail_.userId), userId);
            }
        }
        query.select(root).where(p).orderBy(cb.desc(root.get(AttendanceDingtalkDetail_.userCheckTime)));
        return em.createQuery(query).getResultList();
    }

    /**
     * 企业微信 打卡数据查询
     *
     * @param startTime
     * @param endTime
     * @param userId
     * @return
     * @throws Exception
     */
    public List<AttendanceQywxDetail> findQywxAttendanceDetail(Date startTime, Date endTime, String userId) throws Exception {
        if (startTime == null && endTime == null && userId == null) {
            throw new QywxFindNoArgumentError();
        }
        EntityManager em = this.entityManagerContainer().get(AttendanceQywxDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AttendanceQywxDetail> query = cb.createQuery(AttendanceQywxDetail.class);
        Root<AttendanceQywxDetail> root = query.from(AttendanceQywxDetail.class);

        Predicate p = null;
        if (startTime != null && endTime != null) {
            long start = startTime.getTime();
            long end = endTime.getTime();
            p = cb.between(root.get(AttendanceQywxDetail_.checkin_time), start, end);
        }
        if (userId != null && !userId.isEmpty()) {
            if (p != null) {
                p = cb.and(p, cb.equal(root.get(AttendanceQywxDetail_.userid), userId));
            } else {
                p = cb.equal(root.get(AttendanceQywxDetail_.userid), userId);
            }
        }
        query.select(root).where(p).orderBy(cb.desc(root.get(AttendanceQywxDetail_.checkin_time)));
        return em.createQuery(query).getResultList();

    }


    ////////////////////////////////统计/////////////////////////////

    /**
     * 钉钉考勤 个人统计
     * @param year
     * @param month
     * @param person
     * @param duty
     * @return
     * @throws Exception
     */
    public Long dingdingPersonForMonthDutyTimesCount(String year, String month, String person, String duty) throws Exception {
        Date start = monthFirstDay(year, month);
        Date end = monthLastDay(year, month);
        EntityManager em = this.entityManagerContainer().get(AttendanceDingtalkDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AttendanceDingtalkDetail> root = query.from(AttendanceDingtalkDetail.class);
        Predicate p = cb.between(root.get(AttendanceDingtalkDetail_.userCheckTime), start.getTime(), end.getTime());
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.o2User), person));
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.checkType), duty));
        query.select(cb.count(root)).where(p);
        return em.createQuery(query).getSingleResult();
    }

    public Long dingdingPersonForMonthTimeResultCount(String year, String month, String person, String timeresult) throws Exception {
        Date start = monthFirstDay(year, month);
        Date end = monthLastDay(year, month);
        EntityManager em = this.entityManagerContainer().get(AttendanceDingtalkDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AttendanceDingtalkDetail> root = query.from(AttendanceDingtalkDetail.class);
        Predicate p = cb.between(root.get(AttendanceDingtalkDetail_.userCheckTime), start.getTime(), end.getTime());
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.o2User), person));
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.timeResult), timeresult));
        query.select(cb.count(root)).where(p);
        return em.createQuery(query).getSingleResult();
    }



    /**
     * StatisticDingdingPersonForMonth ids
     * @param year
     * @param month
     * @param person
     * @return
     * @throws Exception
     */
    public List<String> getStatPersonForMonthIds(String year, String month, String person) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingPersonForMonth.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<StatisticDingdingPersonForMonth> root = query.from(StatisticDingdingPersonForMonth.class);
        Predicate p = cb.equal(root.get(StatisticDingdingPersonForMonth_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingPersonForMonth_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingPersonForMonth_.o2User), person));
        query.select(root.get(StatisticDingdingPersonForMonth_.id)).where(p);
        return em.createQuery(query).getResultList();
    }

    /**
     * 钉钉考勤 部门出勤人数 、上班签到人数、下班班签到人数
     *
     * @param date yyyy-MM-dd
     * @param unit 单位
     * @param duty OnDuty OffDuty
     * @return
     * @throws Exception
     */
    public Long dingdingUnitForDayDutyTimesCount(String date, String unit, String duty) throws Exception {
        Date startTime = DateTools.parse(date);
        startTime = startOneDate(startTime);
        Date endTime = endOneDate(startTime);
        EntityManager em = this.entityManagerContainer().get(AttendanceDingtalkDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AttendanceDingtalkDetail> root = query.from(AttendanceDingtalkDetail.class);
        Predicate p = null;
        long start = startTime.getTime();
        long end = endTime.getTime();
        p = cb.between(root.get(AttendanceDingtalkDetail_.userCheckTime), start, end);
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.o2Unit), unit));
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.checkType), duty));
        query.select(cb.count(root)).where(p);
        return em.createQuery(query).getSingleResult();
    }

    public Long dingdingUnitForDayTimeResultCount(String date, String unit, String timeresult) throws Exception {

        Date startTime = DateTools.parse(date);
        startTime = startOneDate(startTime);
        Date endTime = endOneDate(startTime);
        EntityManager em = this.entityManagerContainer().get(AttendanceDingtalkDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<AttendanceDingtalkDetail> root = query.from(AttendanceDingtalkDetail.class);
        Predicate p = null;
        long start = startTime.getTime();
        long end = endTime.getTime();
        p = cb.between(root.get(AttendanceDingtalkDetail_.userCheckTime), start, end);
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.o2Unit), unit));
        p = cb.and(p, cb.equal(root.get(AttendanceDingtalkDetail_.timeResult), timeresult));
        query.select(cb.count(root)).where(p);
        return em.createQuery(query).getSingleResult();
    }

    /**
     * 查询所有有数据的组织 去重的
     * @param date
     * @return
     * @throws Exception
     */
    public List<String> dingdingUnitDistinct(String date) throws Exception {
        Date startTime = DateTools.parse(date);
        startTime = startOneDate(startTime);
        Date endTime = endOneDate(startTime);
        EntityManager em = this.entityManagerContainer().get(AttendanceDingtalkDetail.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<AttendanceDingtalkDetail> root = query.from(AttendanceDingtalkDetail.class);
        Predicate p = cb.between(root.get(AttendanceDingtalkDetail_.userCheckTime), startTime.getTime(), endTime.getTime());
        query.select(root.get(AttendanceDingtalkDetail_.o2Unit)).where(p).distinct(true);
        return em.createQuery(query).getResultList();
    }


    /**
     * 获取StatitsticDingdingForMonth ids
     * @param year
     * @param month
     * @param unit
     * @return
     * @throws Exception
     */
    public List<String> getStatUnitForMonthIds(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForMonth.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<StatisticDingdingUnitForMonth> root = query.from(StatisticDingdingUnitForMonth.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForMonth_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForMonth_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForMonth_.o2Unit), unit));
        query.select(root.get(StatisticDingdingUnitForMonth_.id)).where(p);
        return em.createQuery(query).getResultList();
    }

    public List<String> getStatUnitForDayIds(String year, String month, String day, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticDate), day));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(root.get(StatisticDingdingUnitForDay_.id)).where(p);
        return em.createQuery(query).getResultList();
    }



    public Long sumWorkDayUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.workDayCount))).where(p);
        return em.createQuery(query).getSingleResult();
    }

    public Long sumOnDutyUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.onDutyTimes))).where(p);
        return em.createQuery(query).getSingleResult();
    }
    public Long sumOffDutyUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.offDutyTimes))).where(p);
        return em.createQuery(query).getSingleResult();
    }
    public Long sumLateTimesUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.lateTimes))).where(p);
        return em.createQuery(query).getSingleResult();
    }
    public Long sumLeaveEarlyUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.leaveEarlyTimes))).where(p);
        return em.createQuery(query).getSingleResult();
    }

    public Long sumNotSignedUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.notSignedCount))).where(p);
        return em.createQuery(query).getSingleResult();
    }

    public Long sumAbsenteeismUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.absenteeismTimes))).where(p);
        return em.createQuery(query).getSingleResult();
    }

    public Long sumSeriousLateUnitForDayWithMonth(String year, String month, String unit) throws Exception {
        EntityManager em = this.entityManagerContainer().get(StatisticDingdingUnitForDay.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<StatisticDingdingUnitForDay> root = query.from(StatisticDingdingUnitForDay.class);
        Predicate p = cb.equal(root.get(StatisticDingdingUnitForDay_.statisticYear), year);
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.statisticMonth), month));
        p = cb.and(p, cb.equal(root.get(StatisticDingdingUnitForDay_.o2Unit), unit));
        query.select(cb.sum(root.get(StatisticDingdingUnitForDay_.seriousLateTimes))).where(p);
        return em.createQuery(query).getSingleResult();
    }




    private Date monthLastDay(String year, String month) throws Exception {
        Calendar cal = Calendar.getInstance();
        int yearInt = Integer.parseInt(year);
        cal.set(Calendar.YEAR, yearInt);
        int monthInt = Integer.parseInt(month);
        cal.set(Calendar.MONTH, monthInt);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
    private Date monthFirstDay(String year, String month) throws Exception {
        Calendar cal = Calendar.getInstance();
        int yearInt = Integer.parseInt(year);
        cal.set(Calendar.YEAR, yearInt);
        int monthInt = Integer.parseInt(month);
        cal.set(Calendar.MONTH, monthInt-1);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    private Date startOneDate(Date date) throws Exception {
        return DateTools.floorDate(date, null);
    }

    private Date endOneDate(Date date) throws Exception {
        Calendar cal = DateUtils.toCalendar(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
