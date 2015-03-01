package com.comtop.mynote;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.comtop.mynote.adapter.ItemAdapter;
import com.comtop.mynote.db.DBHelper;
import com.comtop.mynote.db.NoteVO;

public class MainActivity extends FragmentActivity {
	private ListView contentList;
    private TextView queryText;
    private ImageView addNote;
    
	@SuppressLint("NewApi") 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new NoteListFragment()).commit();
		}
		initControl();

	}


	/**
	 * 初始化1
	 */
	private void initControl() {

	}


	@Override
	protected void onResume()   
    {  
		super.onResume();
		contentList = (ListView) this.findViewById(R.id.contentList);
		queryText  = (TextView) this.findViewById(R.id.noteQuerytText1);
		queryText.addTextChangedListener(queryTextChangedListener);
		List<NoteVO> list = this.queryNoteVOList();
	
		ItemAdapter adapter =new ItemAdapter(this, list);
		contentList.setAdapter(adapter);
		contentList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NoteVO temp = (NoteVO)contentList.getItemAtPosition(position);
				//跳转至明细页面
				Intent intent = new Intent(MainActivity.this,NoteDetailActivity.class);
				intent.putExtra("id", temp.getId());
				MainActivity.this.startActivity(intent);
			}
			
		});
		
		//新增操作	
		addNote = (ImageView) this.findViewById(R.id.addNote);
		addNote.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,NoteDetailActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
		
    }
	
	
	private TextWatcher queryTextChangedListener = new TextWatcher(){

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s) {
			String strQuery = queryText.getText().toString().trim();
			List<NoteVO> listQuery;
			if(strQuery.length() > 0){
			  listQuery = DBHelper.getInstance(MainActivity.this).queryNoteVOByTitle(strQuery);
			}else{
			  listQuery =DBHelper.getInstance(MainActivity.this).queryAll();
			}
			
			ItemAdapter adapter =new ItemAdapter(MainActivity.this, listQuery);
			contentList.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			
		}
	};
	
	/**
	 * 查询列表数据
	 * @return
	 */
	private List<NoteVO> queryNoteVOList(){
		return DBHelper.getInstance(this).queryAll();
	}

}
