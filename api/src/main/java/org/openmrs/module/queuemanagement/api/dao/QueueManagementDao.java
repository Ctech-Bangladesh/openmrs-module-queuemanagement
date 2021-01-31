package org.openmrs.module.queuemanagement.api.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.queuemanagement.PatientQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository("queueManagementDao")
public class QueueManagementDao {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public PatientQueue getPatientByIdentifier(String identifier, Date dateCreated) {
		return (PatientQueue) sessionFactory.getCurrentSession().createCriteria(PatientQueue.class)
		        .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq("dateCreated", dateCreated))
		        .add(Restrictions.eq("status", true)).uniqueResult();
	}
	
	@Transactional
	public PatientQueue save(PatientQueue queue) throws Exception {
		System.out.println("Queue to Save ::" + queue);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = dateFormat.parse(dateFormat.format(queue.getDateCreated()));
		System.out.println("Date New :: " + date);
		PatientQueue checkIdentifier = this.getPatientByIdentifier(queue.getIdentifier(), date);
		System.out.println("Data by Identifier: " + checkIdentifier);
		//Queue Count
		List<PatientQueue> queues = this.countIdentifier(queue.getRoomId(), date);
		System.out.println("Queue size in the room: " + queues + " Size:" + queues.size());
		int token = queues.size() + 1;
		if (checkIdentifier != null) {
			this.updateStatus(checkIdentifier);
			
			queue.setToken(token);
			sessionFactory.getCurrentSession().persist(queue);
			System.out.println("Patient Queue Added: " + queue);
		}
		if (queue.getId() == null) {
			queue.setToken(token);
			sessionFactory.getCurrentSession().persist(queue);
			System.out.println("Patient Queue Added: " + queue);
		}
		return queue;
	}
	
	@Transactional
	public PatientQueue updateStatus(PatientQueue queue) {
		System.out.println(queue);
		queue.setStatus(false);
		sessionFactory.getCurrentSession().saveOrUpdate(queue);
		System.out.println("Patient Queue Status Updated: " + queue);
		return queue;
	}
	
	public List<PatientQueue> getPatientQueueByVisitroom(String visitroom, String dateCreated) throws ParseException {
		Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dateCreated);
		System.out.println("Date :: " + d);
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientQueue.class);
		criteria.add(Restrictions.eq("visitroom", visitroom));
		criteria.add(Restrictions.eq("dateCreated", d));
		criteria.add(Restrictions.eq("status", true));
		criteria.setMaxResults(6);
		return criteria.list();
	}
	
	public PatientQueue getTokenByIdentifier(String identifier, Date dateCreated) throws APIException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientQueue.class);
		criteria.add(Restrictions.eq("identifier", identifier));
		criteria.add(Restrictions.eq("dateCreated", dateCreated));
		criteria.add(Restrictions.eq("status", true));
		return (PatientQueue) criteria.uniqueResult();
	}
	
	public List<PatientQueue> getAllQueueId() {
		List<PatientQueue> queueList = sessionFactory.getCurrentSession().createQuery("from PatientQueue").list();
		for (PatientQueue queue : queueList) {
			log.info("Queue List::" + queue);
		}
		return queueList;
	}
	
	public List<PatientQueue> countIdentifier(String roomId, Date d) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientQueue.class);
		criteria.add(Restrictions.eq("roomId", roomId));
		criteria.add(Restrictions.eq("dateCreated", d));
		return criteria.list();
	}
	
	public List<Object> getAllVisitroom() {
		SQLQuery criteria = sessionFactory.getCurrentSession().createSQLQuery(
		    "select distinct visitroom from opd_patient_queue");
		return criteria.list();
	}
	
	@Transactional
	public PatientQueue update(PatientQueue queue) {
		sessionFactory.getCurrentSession().saveOrUpdate(queue);
		System.out.println("Updated Queue :: " + queue);
		return queue;
	}
	
	public void truncate() throws DAOException {
		sessionFactory.getCurrentSession().createSQLQuery("truncate table opd_patient_queue").executeUpdate();
	}
	
	@Transactional
	public PatientQueue getPatientByIdentifierAndVisitroom(String identifier, String roomId, Date dateCreated) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientQueue.class);
		criteria.add(Restrictions.eq("identifier", identifier));
		criteria.add(Restrictions.eq("roomId", roomId));
		criteria.add(Restrictions.eq("dateCreated", dateCreated));
		return (PatientQueue) criteria.uniqueResult();
	}
}
