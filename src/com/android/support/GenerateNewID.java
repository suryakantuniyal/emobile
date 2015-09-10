package com.android.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.database.OrdersHandler;

import android.app.Activity;

public class GenerateNewID {
	private MyPreferences myPref;
	public static final int ORDER_ID = 0, PAYMENT_ID = 1;

	public GenerateNewID(Activity activity) {
		myPref = new MyPreferences(activity);
	}

	public String getNextID(String lastID) {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
		String year = sdf.format(new Date());
		String lastOrderId = OrdersHandler.getLastOrderId(Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
		if (lastID == null)
			lastID = "";

		if (lastID.isEmpty() || lastID.length() <= 4) {
			sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
		} else {
			String delims = "[\\-]";
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
