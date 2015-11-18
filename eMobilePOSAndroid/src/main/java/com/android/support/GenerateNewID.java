package com.android.support;

import android.app.Activity;

import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerateNewID {
	private MyPreferences myPref;
	private Activity activity;

	public static enum IdType {
		ORDER_ID, PAYMENT_ID
	}

	// public static final int ORDER_ID = 0, PAYMENT_ID = 1;
	private static String delims = "[\\-]";

	public GenerateNewID(Activity activity) {
		this.activity = activity;
		myPref = new MyPreferences(activity);
	}

	public String getNextID(String currentId) {
		String[] tokens = currentId.split(delims);
		int seq = Integer.parseInt(tokens[1]);
		seq++;

		return tokens[0] + "-" + String.format("%05d", seq) + "-" + tokens[2];
	}

	public String getNextID(IdType idType) {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
		String year = sdf.format(new Date());
		String lastID = null;

		switch (idType) {
		case ORDER_ID:
			lastID = OrdersHandler.getInstance(activity).getLastOrderId(Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
			break;
		case PAYMENT_ID:
			lastID = PaymentsHandler.getInstance(activity).getLastPaymentId(Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
			break;
		}

		if (lastID == null)
			lastID = "";

		if (lastID.isEmpty() || lastID.length() <= 4) {
			sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
		} else {

			String[] tokens = lastID.split(delims);

			if (tokens[2].equals(year)) {
				int seq = Integer.parseInt(tokens[1]);
				sb.append(myPref.getEmpID()).append("-").append(String.format("%05d", (seq + 1))).append("-")
						.append(year);
			} else {
				sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
			}
		}

		return sb.toString();
	}

	// public String generate(String lastID,int type) {
	// StringBuilder sb = new StringBuilder();
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy",Locale.getDefault());
	// String year = sdf.format(new Date());
	// //String lastPayID = myPref.getLastPayID();
	//
	// if(lastID==null)
	// lastID = "";
	//
	// switch(type)
	// {
	// case 0://For ord_id
	// String lastOrdID = myPref.getLastOrdID();
	//
	// if((lastID!=null&&!lastID.isEmpty())||(!lastOrdID.isEmpty()&&!lastOrdID.equals("0")))
	// {
	// if(lastID.isEmpty()&&!lastOrdID.isEmpty()&&!lastOrdID.equals("0"))
	// lastID = lastOrdID;
	//
	// String delims = "[\\-]";
	// String[] tokens = lastID.split(delims);
	//
	//
	// if(tokens[2].equals(year))
	// {
	// int seq = Integer.parseInt(tokens[1]);
	// sb.append(myPref.getEmpID()).append("-").append(String.format("%05d",
	// (seq + 1))).append("-").append(year);
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// }
	// else
	// {
	// //if(lastOrdID==null||lastOrdID.isEmpty()||lastOrdID.equals("0"))
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	//
	//
	// break;
	// case 1://For pay_id
	// String lastPayID = myPref.getLastPayID();
	// if((!lastID.isEmpty())||(!lastPayID.isEmpty()&&!lastPayID.equals("0")))
	// {
	// if(lastID.isEmpty()&&!lastPayID.isEmpty()&&!lastPayID.equals("0"))
	// lastID = lastPayID;
	//
	// String delims = "[\\-]";
	// String[] tokens = lastID.split(delims);
	//
	// if(tokens[2].equals(year))
	// {
	//
	// int seq = Integer.parseInt(tokens[1]);
	// sb.append(myPref.getEmpID()).append("-").append(String.format("%05d",
	// (seq + 1))).append("-").append(year);
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// //myPref.setLastPayID(sb.toString());
	// }
	// break;
	// case 2://For cust_id (inserting new customer)
	// if(!lastID.isEmpty())
	// {
	// String delims = "[\\-]";
	// String[] tokens = lastID.split(delims);
	// if(tokens[2].equals(year))
	// {
	//
	// int seq = Integer.parseInt(tokens[1]);
	// sb.append(myPref.getEmpID()).append("-").append(String.format("%05d",
	// (seq + 1))).append("-").append(year);
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// }
	// else
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// break;
	//
	// case 3:
	// String lastConsTransID = myPref.getLastConsTransID();
	// if((!lastID.isEmpty())||(!lastConsTransID.isEmpty()&&!lastConsTransID.equals("0")))
	// {
	// if(lastID.isEmpty()&&!lastConsTransID.isEmpty()&&!lastConsTransID.equals("0"))
	// lastID = lastConsTransID;
	//
	// String delims = "[\\-]";
	// String[] tokens = lastID.split(delims);
	// if(tokens[2].equals(year))
	// {
	//
	// int seq = Integer.parseInt(tokens[1]);
	// sb.append(myPref.getEmpID()).append("-").append(String.format("%05d",
	// (seq + 1))).append("-").append(year);
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// break;
	// case 4:
	// String lastTransferID = myPref.getLastTransferID();
	// if((!lastID.isEmpty())||(!lastTransferID.isEmpty()&&!lastTransferID.equals("0")))
	// {
	// if(lastID.isEmpty()&&!lastTransferID.isEmpty()&&!lastTransferID.equals("0"))
	// lastID = lastTransferID;
	//
	// String delims = "[\\-]";
	// String[] tokens = lastID.split(delims);
	// if(tokens[2].equals(year))
	// {
	//
	// int seq = Integer.parseInt(tokens[1]);
	// sb.append(myPref.getEmpID()).append("-").append(String.format("%05d",
	// (seq + 1))).append("-").append(year);
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// }
	// else
	// {
	// sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
	// }
	// break;
	// }
	// /*if (!lastID.isEmpty()) {
	// String delims = "[\\-]";
	// String[] tokens = lastID.split(delims);
	// int seq = Integer.parseInt(tokens[1]);
	// sb.append(myPref.getEmpID()).append("-").append(String.format("%05d",
	// (seq + 1))).append("-").append(year);
	// } else {
	// sb.append(myPref.getEmpID()).append("-").append("00000").append("-").append(year);
	// myPref.setLastPayID(sb.toString());
	// }*/
	//
	// //String t = sb.toString();
	// return sb.toString();
	//
	// }
}
