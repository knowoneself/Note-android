package com.comtop.mynote.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.comtop.mynote.R;
import com.comtop.mynote.db.NoteVO;
import com.comtop.mynote.utils.CommonUtil;
import com.comtop.mynote.utils.Constants;
import com.comtop.mynote.utils.DateUtil;

public class ItemAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<NoteVO> list;
	private Context context;
	public ItemAdapter(Context context, List<NoteVO> list) {
		inflater = LayoutInflater.from(context);
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		return (list != null && list.size() != 0) ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return (list != null && list.size() != 0) ? list.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewItem item = null;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.activity_list_item, null);
			item = new ViewItem();
			item.date = (TextView) convertView.findViewById(R.id.itme_date);
			item.title = (TextView) convertView.findViewById(R.id.itme_title);
			item.itmeImg = (ImageView) convertView.findViewById(R.id.itme_img);
			convertView.setTag(item);
		} else {
			item = (ViewItem) convertView.getTag();
		}
		NoteVO entity = list.get(position);

		item.title.setText(entity.getTitle());
		if(null !=entity.getModifyDate()){
			item.date.setText(DateUtil.getFormatDateTime(entity.getModifyDate(), "yyyy-MM-dd HH:mm:ss"));
		}
		
		//如果存在录音则显示带录音的图标
		String imgPath = CommonUtil.getSdCardPath() + Constants.VOICE_PATH + entity.getId()+".amr";
		if(CommonUtil.isFileExists(imgPath)){
			item.itmeImg.setImageDrawable(context.getResources().getDrawable(R.drawable.note_voiceandtext));
		}
		return convertView;
	}

	public final class ViewItem {
		public TextView title;
		public TextView date;
		public ImageView itmeImg;
	}

}
