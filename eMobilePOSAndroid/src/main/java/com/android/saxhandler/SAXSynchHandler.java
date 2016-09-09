package com.android.saxhandler;

import android.app.Activity;

import com.android.database.AddressHandler;
import com.android.database.CategoriesHandler;
import com.android.database.ClerksHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.database.CustomersHandler;
import com.android.database.DeviceDefaultValuesHandler;
import com.android.database.DrawInfoHandler;
import com.android.database.EmpInvHandler;
import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.database.LocationsInventory_DB;
import com.android.database.Locations_DB;
import com.android.database.MemoTextHandler;
import com.android.database.OrdProdAttrList_DB;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PriceLevelHandler;
import com.android.database.PriceLevelItemsHandler;
import com.android.database.ProdCatXrefHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductAliases_DB;
import com.android.database.ProductChainXrefHandler;
import com.android.database.ProductsAttrHandler;
import com.android.database.ProductsHandler;
import com.android.database.ProductsImagesHandler;
import com.android.database.SalesTaxCodesHandler;
import com.android.database.ShipMethodHandler;
import com.android.database.TaxesGroupHandler;
import com.android.database.TaxesHandler;
import com.android.database.TemplateHandler;
import com.android.database.TermsAndConditionsHandler;
import com.android.database.TermsHandler;
import com.android.database.UOMHandler;
import com.android.database.VolumePricesHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SAXSynchHandler extends DefaultHandler {

	private Activity activity;
	private boolean isTable;
	private boolean isAttribute = false;
	private static List<String> list_data;
	private static final String empStr = "";
	private static String start_tag;
	private HashMap<String, Integer> temp_data;
	private List<HashMap<String,Integer>> dictionaryListMap;
	private List<String[]>dataList;
	private int outterCounter = 0;
	private StringBuilder data;

	private int counter = 0;
	private int synchType;
	
	
	//SQL Table Handler
	private CustomersHandler custHandler;
	private AddressHandler addrHandler;
	private CategoriesHandler catHandler;
	private EmpInvHandler empInvHandler;
	private InvProdHandler prodInvHandler;
	private InvoicesHandler invHandler;
	private ProdCatXrefHandler prodCatHandler;
	private ProductChainXrefHandler prodChainHandler;
	private ProductsImagesHandler prodImgHandler;
	private SalesTaxCodesHandler salesTaxCodesHandler;
	private ShipMethodHandler shipMethodHandler;
	private TaxesHandler taxesHandler;
	private TaxesGroupHandler taxesGroupHandler;
	private TermsHandler termsHandler;
	private MemoTextHandler memoTxtHandler;
	private DeviceDefaultValuesHandler deviceHandler;
	private VolumePricesHandler vpHandler;
	private TemplateHandler templatesHandler;
	private DrawInfoHandler drawInfoHandler;
	private CustomerInventoryHandler custInventoryHandler;
	private ProductsAttrHandler productsAttrHandler;
	private ClerksHandler clerksHandler;
	private TermsAndConditionsHandler termsAndConditionsHandler;
	private OrdersHandler ordersHandler;
	private OrderProductsHandler orderProdHandler;
	private Locations_DB locationsDB;
	private LocationsInventory_DB locationsInventoryDB;
	
	public enum Limiters {
		Table;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}


	public SAXSynchHandler(Activity activity,int syncType) {
		this.activity = activity;
		//this.db = db;
		this.synchType = syncType;
		temp_data = new HashMap<>();
		list_data = new ArrayList<>();
		data = new StringBuilder();
		
		dictionaryListMap = new ArrayList<>();
		dataList = new ArrayList<>();
		
		
		switchCase(false);
	}

	@Override
	public void startDocument() throws SAXException {
		list_data = new ArrayList<>();

	}
	
	@Override
	public void endDocument() throws SAXException {
		if(dictionaryListMap.size()>0)
			switchCase(true);
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Limiters test = Limiters.toLimit(localName);

		if (test != null) {
			switch (test) {
			case Table: {
				isTable = true;
				temp_data = new HashMap<>();
				break;
			}
			}

		} else if (isTable) {
			start_tag = localName;
			isAttribute = true;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		Limiters test = Limiters.toLimit(localName);
		if (test != null) {
			switch (test) {
			case Table: {

				counter = 0;
				isTable = false;
				isAttribute = false;
				outterCounter++;
				
				dictionaryListMap.add(temp_data);
				dataList.add(list_data.toArray(new String[list_data.size()]));
				
				if(outterCounter==Global.sqlLimitTransaction)
				{
					switchCase(true);
					dictionaryListMap.clear();
					dataList.clear();
					outterCounter = 0;
				}
				
				list_data = new ArrayList<>();
				
				
				break;
			}
			}
		} else if (isTable) {
			temp_data.put(start_tag, counter);
			list_data.add(data.toString());

			counter++;
			int size = list_data.size();

			if (size != counter) {
				temp_data.put(localName, counter - 1);
				list_data.add(empStr);
			}
			isAttribute = false;

			data = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		//tag = tag.trim();
		if (isTable && isAttribute) {
			data.append(tag);
		}
	}
	
	private void switchCase (boolean isInsert)
	{
		switch(this.synchType)
		{
		case Global.S_CUSTOMERS:
			if(isInsert)
				custHandler.insert(dataList,dictionaryListMap);
			else
			{
				custHandler = new CustomersHandler(activity);
				custHandler.emptyTable();
			}
			break;
		case Global.S_ADDRESS:
			if(isInsert)
				addrHandler.insert(dataList,dictionaryListMap);
			else
			{
				addrHandler = new AddressHandler(activity);
				addrHandler.emptyTable();
			}
			break;
		case Global.S_CATEGORIES:
			if(isInsert)
				catHandler.insert(dataList,dictionaryListMap);
			else
			{
				catHandler = new CategoriesHandler(activity);
				catHandler.emptyTable();
			}
			break;
		case Global.S_EMPLOYEE_INVOICES:
			if(isInsert)
				empInvHandler.insert(dataList,dictionaryListMap);
			else
			{
				empInvHandler = new EmpInvHandler(activity);
				empInvHandler.emptyTable();
			}
			break;
		case Global.S_PRODUCTS_INVOICES:
			if(isInsert)
				prodInvHandler.insert(dataList,dictionaryListMap);
			else
			{
				prodInvHandler = new InvProdHandler(activity);
				prodInvHandler.emptyTable();
			}
			break;
		case Global.S_INVOICES:
			if(isInsert)
				invHandler.insert(dataList,dictionaryListMap);
			else
			{
				invHandler = new InvoicesHandler(activity);
				invHandler.emptyTable();
			}
			break;
		case Global.S_PRODCATXREF:
			if(isInsert)
				prodCatHandler.insert(dataList,dictionaryListMap);
			else
			{
				prodCatHandler = new ProdCatXrefHandler(activity);
				prodCatHandler.emptyTable();
			}
			break;
		case Global.S_PROD_CHAIN:
			if(isInsert)
				prodChainHandler.insert(dataList,dictionaryListMap);
			else
			{
				prodChainHandler = new ProductChainXrefHandler(activity);
				prodChainHandler.emptyTable();
			}
			break;
		case Global.S_PROD_IMG:
			if(isInsert)
				prodImgHandler.insert(dataList,dictionaryListMap);
			else
			{
				prodImgHandler = new ProductsImagesHandler(activity);
				prodImgHandler.emptyTable();
			}
			break;
		case Global.S_SALES_TAX_CODE:
			if(isInsert)
				salesTaxCodesHandler.insert(dataList,dictionaryListMap);
			else
			{
				salesTaxCodesHandler = new SalesTaxCodesHandler(activity);
				salesTaxCodesHandler.emptyTable();
			}
			break;
		case Global.S_SHIP_METHODS:
			if(isInsert)
				shipMethodHandler.insert(dataList,dictionaryListMap);
			else
			{
				shipMethodHandler = new ShipMethodHandler(activity);
				shipMethodHandler.emptyTable();
			}
			break;
		case Global.S_TAXES:
			if(isInsert)
				taxesHandler.insert(dataList,dictionaryListMap);
			else
			{
				taxesHandler = new TaxesHandler(activity);
				taxesHandler.emptyTable();
			}
			break;
		case Global.S_TAX_GROUP:
			if(isInsert)
				taxesGroupHandler.insert(dataList,dictionaryListMap);
			else
			{
				taxesGroupHandler = new TaxesGroupHandler(activity);
				taxesGroupHandler.emptyTable();
				
			}
			break;
		case Global.S_TERMS:
			if(isInsert)
				termsHandler.insert(dataList,dictionaryListMap);
			else
			{
				termsHandler = new TermsHandler(activity);
				termsHandler.emptyTable();
			}
			break;
		case Global.S_MEMO_TXT:
			if(isInsert)
				memoTxtHandler.insert(dataList,dictionaryListMap);
			else
			{
				memoTxtHandler = new MemoTextHandler(activity);
				memoTxtHandler.emptyTable();
			}
			break;

		case Global.S_VOLUME_PRICES:
			if(isInsert)
				vpHandler.insert(dataList,dictionaryListMap);
			else
			{
				vpHandler = new VolumePricesHandler(activity);
				vpHandler.emptyTable();
			}
			break;
		case Global.S_TEMPLATES:
			if(isInsert)
				templatesHandler.insert(dataList,dictionaryListMap);
			else
			{
				templatesHandler = new TemplateHandler(activity);
				templatesHandler.emptyTable();
			}
			break;
		case Global.S_IVU_LOTTO:
			if(isInsert)
				drawInfoHandler.insert(dataList,dictionaryListMap);
			else
			{
				drawInfoHandler = new DrawInfoHandler(activity);
				drawInfoHandler.emptyTable();
			}
			break;
		case Global.S_DEVICE_DEFAULT_VAL:
			if(isInsert)
				deviceHandler.insert(dataList,dictionaryListMap);
			else
			{
				deviceHandler = new DeviceDefaultValuesHandler(activity);
				deviceHandler.emptyTable();
			}
			break;
		case Global.S_CUSTOMER_INVENTORY:
			if(isInsert)
				custInventoryHandler.insert(dataList,dictionaryListMap);
			else
			{
				custInventoryHandler = new CustomerInventoryHandler(activity);
				custInventoryHandler.emptyTable();
			}
			break;
		case Global.S_CONSIGNMENT_TRANSACTION:
			if(isInsert)
			{
				int size = dataList.size();
				if(size>0)
				{
					MyPreferences myPref = new MyPreferences(activity);
					myPref.setLastConsTransID(dataList.get(0)[dictionaryListMap.get(0).get("ConsTrans_ID")]);
				}
			}
			break;
			
		case Global.S_PRODUCTS_ATTRIBUTES:
			if(isInsert)
				productsAttrHandler.insert(dataList,dictionaryListMap);
			else
			{
				productsAttrHandler = new ProductsAttrHandler(activity);
				productsAttrHandler.emptyTable();
			}
			break;
		case Global.S_CLERKS:
			if(isInsert)
				clerksHandler.insert(dataList,dictionaryListMap);
			else
			{
				clerksHandler = new ClerksHandler(activity);
				clerksHandler.emptyTable();
			}
			break;
		case Global.S_TERMS_AND_CONDITIONS:
			if(isInsert)
				termsAndConditionsHandler.insert(dataList,dictionaryListMap);
			else
			{
				termsAndConditionsHandler = new TermsAndConditionsHandler(activity);
				termsAndConditionsHandler.emptyTable();
			}
			break;
		case Global.S_ORDERS_ON_HOLD_LIST:
			if(isInsert)
				ordersHandler.insertOnHold(dataList,dictionaryListMap);
			else
			{
				ordersHandler = new OrdersHandler(activity);
				ordersHandler.emptyTableOnHold();
			}
			break;
		case Global.S_ORDERS_ON_HOLD_DETAILS:
			if(isInsert)
				orderProdHandler.insertOnHold(dataList, dictionaryListMap);
			else
				orderProdHandler = new OrderProductsHandler(activity);
			break;
		case Global.S_LOCATIONS:
			if(isInsert)
				locationsDB.insert(dataList, dictionaryListMap);
			else
			{
				locationsDB = new Locations_DB(activity);
				locationsDB.emptyTable();
			}
			break;
		case Global.S_LOCATIONS_INVENTORY:
			if(isInsert)
				locationsInventoryDB.insert(dataList, dictionaryListMap);
			else
			{
				locationsInventoryDB = new LocationsInventory_DB(activity);
				locationsInventoryDB.emptyTable();
			}
			break;
		}
	}
}
