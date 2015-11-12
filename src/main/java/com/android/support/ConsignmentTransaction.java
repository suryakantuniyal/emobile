package com.android.support;


public class ConsignmentTransaction 
{
	private final String empt = "";
	
	public String Cons_ID  = empt;
	public String ConsTrans_ID = empt;
	public String ConsEmp_ID = empt;
	public String ConsCust_ID = empt;
	public String ConsInvoice_ID = empt;
	public String ConsReturn_ID = empt;
	public String ConsPickup_ID = empt;
	public String ConsDispatch_ID = empt;
	public String ConsInventory_Qty = empt;
	public String ConsProd_ID = empt;
	public String ConsOriginal_Qty = "0";
	public String ConsStock_Qty = "0";
	public String ConsInvoice_Qty = "0";
	public String ConsReturn_Qty = "0";
	public String ConsDispatch_Qty = "0";
	public String ConsPickup_Qty = "0";
	public String ConsNew_Qty = "0";
	public String Cons_timecreated = empt;
	public String is_synched = "0";
	
	public String invoice_total = "0";
	
	public ConsignmentTransaction()
	{
		Cons_timecreated = Global.getCurrentDate();
	}
	
/*	private enum Limiters {
		Cons_ID,ConsTrans_ID,ConsEmp_ID,ConsCust_ID,ConsInvoice_ID,ConsReturn_ID,ConsPickup_ID,ConsDispatch_ID,
		ConsProd_ID,ConsOriginal_Qty,ConsStock_Qty,ConsInvoice_Qty,ConsReturn_Qty,ConsDispatch_Qty,ConsPickup_Qty,ConsNew_Qty,
		Cons_timecreated,invoice_total,is_synched;

		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}
	
	public String getSetData(String attribute, boolean get, String value) 
	{
		Limiters test = Limiters.toLimit(attribute);
		String returnedVal = empt;
		if(value == null)
			value = empt;
		if (test != null) 
		{
			switch (test) 
			{
			case Cons_ID:
				if (get)
					returnedVal = this.Cons_ID;
				else
					this.Cons_ID = value;
				break;
			case ConsTrans_ID:
				if(get)
					returnedVal = this.ConsTrans_ID;
				else
					this.ConsTrans_ID = value;
				break;
			case ConsEmp_ID:
				if(get)
					returnedVal = this.ConsEmp_ID;
				else
					this.ConsEmp_ID = value;
				break;
			case ConsCust_ID:
				if(get)
					returnedVal = this.ConsCust_ID;
				else
					this.ConsCust_ID = value;
				break;
			case ConsInvoice_ID:
				if(get)
					returnedVal = this.ConsInvoice_ID;
				else
					this.ConsInvoice_ID = value;
				break;
			case ConsReturn_ID:
				if(get)
					returnedVal = this.ConsReturn_ID;
				else
					this.ConsReturn_ID = value;
				break;
			case ConsPickup_ID:
				if(get)
					returnedVal = this.ConsPickup_ID;
				else
					this.ConsPickup_ID = value;
				break;
			case ConsDispatch_ID:
				if(get)
					returnedVal = this.ConsDispatch_ID;
				else
					this.ConsDispatch_ID = value;
				break;
			case ConsProd_ID:
				if(get)
					returnedVal = this.ConsProd_ID;
				else
					this.ConsProd_ID = value;
				break;
			case ConsOriginal_Qty:
				if(get)
				{
					returnedVal = this.ConsOriginal_Qty;
					if(returnedVal.isEmpty())
						returnedVal = "0";
				}
				else
					this.ConsOriginal_Qty = value;
				break;
			case ConsStock_Qty:
				if(get)
				{
					returnedVal = this.ConsStock_Qty;
					if(returnedVal.isEmpty())
						returnedVal = "0";
				}
				else
					this.ConsStock_Qty = value;
				break;
			case ConsInvoice_Qty:
				if(get)
				{
					returnedVal = this.ConsInvoice_Qty;
					if(returnedVal.isEmpty())
						returnedVal = "0";
				}
				else
					this.ConsInvoice_Qty = value;
				break;
			case ConsReturn_Qty:
				if(get)
				{
					returnedVal = this.ConsReturn_Qty;
					if(returnedVal.isEmpty())
						returnedVal = "0";
				}
				else
					this.ConsReturn_Qty = value;
				break;
			case ConsDispatch_Qty:
				if(get)
				{
					returnedVal = this.ConsDispatch_Qty;
					if(returnedVal.isEmpty())
						returnedVal = "0";
				}
				else
					this.ConsDispatch_Qty = value;
				break;
			case ConsPickup_Qty:
				if(get)
				{
					returnedVal = this.ConsPickup_Qty;
					if(returnedVal.isEmpty())
						returnedVal = "0";
				}
				else
					this.ConsPickup_Qty = value;
				break;
			case ConsNew_Qty:
				if(get)
					returnedVal = this.ConsNew_Qty;
				else
					this.ConsNew_Qty = value;
				break;
			case Cons_timecreated:
				if(get)
					returnedVal= this.Cons_timecreated;
				else
					this.Cons_timecreated = value;
				break;
			case invoice_total:
				if(get)
				{
					returnedVal = this.invoice_total;
					if(returnedVal.isEmpty())
						returnedVal = "0.00";
				}
				else
					this.invoice_total = value;
				break;
			case is_synched:
				if(get)
					returnedVal = this.is_synched;
				else
					this.is_synched = value;
				break;
			}
		}
		
		if(returnedVal == null)
			returnedVal = empt;
		
		return returnedVal;
	}*/
}
