package model.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.rest.utils.RestUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class PrescriptionManager {

	public static List<Prescription> getValidPrescriptions(Session session,
			int clinicId, boolean isPaeds, Date startDate, Date endDate, Date cutOff) {
		return getValidPrescriptions(session, clinicId, isPaeds, startDate, endDate,
				cutOff, false);
	}
	
	public static List<Prescription> getValidPrescriptions(Session session,
			int clinicId, boolean isPaeds, Date startDate, Date endDate,
			Date cutOff, boolean limitByEpisodes) {

		Criteria criteria;

		if (clinicId != 0) {

			criteria = session.createCriteria(Prescription.class).createAlias(
					"patient", "patient").createAlias("patient.episodes",
					"episode").createAlias("episode.clinic", "clinic").add(
					Restrictions.or(Restrictions.and(Restrictions.le("date",
							endDate), Restrictions.isNull("endDate")),
							Restrictions.and(Restrictions.le("date", endDate),
									Restrictions.ge("endDate", endDate)))).add(
					Restrictions.eq("clinic.id", clinicId));
		} else {
			criteria = session.createCriteria(Prescription.class).createAlias(
					"patient", "patient").createAlias("patient.episodes",
					"episode").add(
					Restrictions.or(Restrictions.and(Restrictions.le("date",
							endDate), Restrictions.isNull("endDate")),
							Restrictions.and(Restrictions.le("date", endDate),
									Restrictions.ge("endDate", endDate))));				
		}

		if (cutOff != null) {
			if (isPaeds) {
				criteria.add(Restrictions.gt("patient.dateOfBirth", cutOff));
			} else {
				criteria.add(Restrictions.le("patient.dateOfBirth", cutOff));
			}
		}

		if (limitByEpisodes && startDate != null && startDate.before(endDate)) {
			criteria.add(
					Restrictions.or(
					Restrictions.or(Restrictions.between("episode.startDate", startDate, endDate), 
							Restrictions.between("episode.stopDate", startDate, endDate)),
							Restrictions.and(Restrictions.lt("episode.startDate", startDate),
									Restrictions.isNull("episode.stopDate"))));
		}

		 criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<Prescription> scripts = criteria.list();
		return scripts;
	}

	public static Map<Regimen, Set<Integer>> getRegimenIdMap(Session session) {
		List<Regimen> regimens = session.createQuery("from Regimen as reg")
				.list();
		Iterator<Regimen> regIt = regimens.iterator();
		Map<Regimen, Set<Integer>> regimenIdMap = new HashMap<Regimen, Set<Integer>>();
		while (regIt.hasNext()) {
			Set<Integer> regDrugSet = new HashSet<Integer>();

			// get all the drugs in this set of regimenDrugs
			Regimen theReg = regIt.next();

			List<RegimenDrugs> regDrugs = theReg.getRegimenDrugs();

			Iterator<RegimenDrugs> regDrugsIt = regDrugs.iterator();

			while (regDrugsIt.hasNext()) {
				RegimenDrugs rd = regDrugsIt.next();
				// we only care about ARVs
				if (rd.getDrug().isARV()) {
					regDrugSet.add(rd.getDrug().getId());
				}
			}

			if (!regDrugSet.isEmpty()) {
				regimenIdMap.put(theReg, regDrugSet);
			}
		}
		return regimenIdMap;
	}

	/**
	 * This method will return the first prescription
	 * 
	 * @return
	 */
	public static Prescription getFirstPrescriptionForEpisode(Session session,
			Episode episode) {

		if (episode.getStopDate() == null) {
			return (Prescription) session.createQuery(
					"select pre from Prescription pre where "
							+ "pre.patient = :pId and "
							+ "(pre.endDate > :startDate  or "
							+ "pre.endDate is null ) "
							+ "order by pre.date asc").setLong("pId",
					episode.getPatient().getId()).setDate("startDate",
					episode.getStartDate()).setMaxResults(1).uniqueResult();
		}

		return (Prescription) session.createQuery(
				"select pre from Prescription pre where "
						+ "pre.patient = :pId and "
						+ "((pre.date between :startDate and :endDate) or "
						+ "(pre.endDate between :startDate and :endDate) or "
						+ "(pre.date < :startDate and pre.endDate is null)) "
						+ "order by pre.date asc").setLong("pId",
				episode.getPatient().getId()).setDate("startDate",
				episode.getStartDate()).setDate("endDate",
				episode.getStopDate()).setMaxResults(1).uniqueResult();

	}

	/**
	 * @param pres
	 * @return a string of the form d4t 30, EFV 600 etc
	 * @throws HibernateException
	 */
	public static String getShortPrescriptionContentsString(Prescription pres) {
		String drugsInPrescription = "";

		List<Drug> result = new ArrayList<Drug>();
		if (pres.getPrescribedDrugs() != null) {
			for (PrescribedDrugs pd : pres.getPrescribedDrugs()) {
				result.add(pd.getDrug());
			}

			drugsInPrescription = DrugManager.getDrugListString(result, ", ", true);
		}

		return drugsInPrescription;
	}

	/**
	 * This method will return the first prescription
	 * 
	 * @return
	 */
	public static Prescription getLastPrescriptionForEpisode(Session session,
			Episode episode) {

		return (Prescription) session.createQuery(
				"select pre from Prescription pre where "
						+ "pre.patient = :pId and "
						+ "((pre.date between :startDate and :endDate) or "
						+ "(pre.endDate between :startDate and :endDate) or "
						+ "(pre.date < :startDate and pre.endDate is null)) "
						+ "order by pre.date asc").setLong("pId",
				episode.getPatient().getId()).setDate("startDate",
				episode.getStartDate()).setDate("endDate",
				episode.getStopDate()).setMaxResults(1).uniqueResult();

	}

	@SuppressWarnings("unchecked")
	public static Doctor getProvider(Session session) throws HibernateException {
		Doctor id = null;
		List<Doctor> doc = null;
		doc = session.createQuery(
				"select doctor from Doctor as doctor "
						+ "where doctor.firstname = 'Provider' OR doctor.firstname = 'Provedor' OR doctor.lastname = 'Provider' OR doctor.lastname = 'Provedor'").list();

		Iterator<Doctor> iter = doc.iterator();
		if (iter.hasNext()) {
			id = iter.next();
		}
		return id;
	}

	// Devolve a lista de todos FILAS de pacientes prontos para enviar ao OpenMRS (Estado do paciente P- Pronto, E- Exportado)
	public static List<SyncOpenmrsDispense> getAllSyncOpenmrsDispenseReadyToSaveByUUID(Session sess, String uuid) throws HibernateException {
		List result;
		result = sess.createQuery("from SyncOpenmrsDispense sync where sync.uuid = '"+uuid+"'").list();
		return result;
	}

	// Devolve a lista de todos FILAS de pacientes prontos para enviar ao OpenMRS (Estado do paciente P- Pronto, E- Exportado)
	public static List<SyncOpenmrsDispense> getAllSyncOpenmrsDispenseReadyToSave(Session sess) throws HibernateException {
		List result;
		result = sess.createQuery("from SyncOpenmrsDispense sync where sync.syncstatus = 'P'").list();
		return result;
	}

	// Devolve a lista de receitas de pacientes por enviar
	public static SyncOpenmrsDispense getSyncOpenmrsDispenseByPrescription(Session sess, Prescription prescription, String pickupDate) throws HibernateException {
		SyncOpenmrsDispense result;

		List syncDispense = sess.createQuery("from SyncOpenmrsDispense sync where sync.prescription = '" + prescription.getId()+"' and strPickUp = '"+pickupDate+"' ").list();

		if (syncDispense.isEmpty())
			result = null;
		else
			result = (SyncOpenmrsDispense) syncDispense.get(0);

		return result;
	}

	public static void saveSyncOpenmrsPatienFila(Session s, SyncOpenmrsDispense SyncOpenmrsDispense)
			throws HibernateException {

		s.saveOrUpdate(SyncOpenmrsDispense);
	}

	public static void setUUIDSyncOpenmrsPatienFila(Session s,  SyncOpenmrsDispense syncOpenmrsDispense)
			throws HibernateException {

		s.update(syncOpenmrsDispense);
	}


}
