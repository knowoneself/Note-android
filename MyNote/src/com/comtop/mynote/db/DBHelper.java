package com.comtop.mynote.db;

import java.util.Date;
import java.util.List;

import android.content.Context;

import com.comtop.mynote.db.DaoMaster.OpenHelper;
import com.comtop.mynote.utils.Constants;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition.StringCondition;

public class DBHelper {

	private static DaoMaster daoMaster;
	private static DaoSession daoSession;
	private static DBHelper instance;
	private static Context mContext;

	/** Note Dao */
	private static NoteVODao noteVODao;

	public static DaoMaster getDaoMaster(Context context) {
		OpenHelper helper = new DaoMaster.DevOpenHelper(context,
				Constants.MY_NOTE_DB_NAME, null);
		daoMaster = new DaoMaster(helper.getWritableDatabase());
		return daoMaster;
	}

	public static DaoSession getDaoSession(Context context) {
		daoMaster = getDaoMaster(context);
		daoSession = daoMaster.newSession();

		return daoSession;
	}

	public static DBHelper getInstance(Context context) {
		instance = new DBHelper();
		mContext = context;
		// 得到Session
		DaoSession daoSession = getDaoSession(mContext);
		noteVODao = daoSession.getNoteVODao();
		return instance;
	}

	/***
	 * 读取Note
	 * 
	 * @param objNoteVO
	 */
	public NoteVO readNoteVO(Long id) {

		return noteVODao.load(id);

	}

	/***
	 * 更新Note
	 * 
	 * @param objNoteVO
	 */
	public void updateNoteVO(NoteVO objNoteVO) {
		if (objNoteVO != null) {
			objNoteVO.setModifyDate(new Date(System.currentTimeMillis()));
			noteVODao.update(objNoteVO);
		}
	}

	/**
	 * 删除Note
	 * 
	 * @param objNoteVO
	 */
	public void deleteNoteVO(Long key) {
			noteVODao.deleteByKey(key);
	}

	/**
	 * 插入单个Note
	 * 
	 * @param objNoteVO
	 */
	public Long insertNoteVO(NoteVO objNoteVO) {
		if (objNoteVO != null) {
			objNoteVO.setCreateDate(new Date(System.currentTimeMillis()));
			objNoteVO.setModifyDate(new Date(System.currentTimeMillis()));
			return noteVODao.insert(objNoteVO);
		}
		return (long) 0;
	}

	/**
	 * 删除所有记录
	 */
	public void deleteAllNotes() {
		noteVODao.deleteAll();
	}

	/**
	 * 查询所有记录 按ModifyDate排序
	 * @return
	 */
	public List<NoteVO> queryAll(){
		
		QueryBuilder<NoteVO> qb = noteVODao.queryBuilder();
		qb.orderDesc(NoteVODao.Properties.ModifyDate);
		//return noteVODao.loadAll();
		return qb.list();
	}
	
	/**
	 * 查询条件获取记录 按ModifyDate排序
	 * @return
	 */
	public List<NoteVO> queryNoteVOByTitle(String query){
		 QueryBuilder<NoteVO> qb = noteVODao.queryBuilder();
			StringBuffer sbSQL = new StringBuffer(50);
			sbSQL.append(" TITLE LIKE '%");   // title
			sbSQL.append(query);
			sbSQL.append("%' ORDER BY MODIFY_DATE "); //modifyDate
			qb.where(new StringCondition(sbSQL.toString())).build();
		return qb.list();
	}
}
