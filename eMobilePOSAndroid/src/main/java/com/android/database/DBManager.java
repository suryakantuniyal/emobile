package com.android.database;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.text.TextUtils;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.SynchMethods;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.io.FileUtils;
import org.kobjects.base64.Base64;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DBManager {
    public static final int VERSION = 61;
    public static final String DB_NAME_OLD = "emobilepos.sqlite";
    private static final String CIPHER_DB_NAME = "emobilepos.sqlcipher";
    private static final String PASSWORD = "em0b1l3p05";
    //    private boolean sendAndReceive = false;
    private static SQLiteDatabase database;
    private final String[] CREATE_INDEX = {
            "CREATE INDEX IF NOT EXISTS prod_id_index ON EmpInv (prod_id)",
            "CREATE INDEX IF NOT EXISTS locationsinventory_prod_id_index ON LocationsInventory (prod_id)",
            "CREATE INDEX IF NOT EXISTS orderproduct_prod_id_index ON OrderProduct (prod_id)",
            "CREATE INDEX IF NOT EXISTS orderproduct_prod_id_index ON OrderProduct (cat_id)",
            "CREATE INDEX IF NOT EXISTS orderproduct_isAdded_index ON OrderProduct (isAdded)",
            "CREATE INDEX IF NOT EXISTS orderproduct_prod_taxId_index ON OrderProduct (prod_taxId)",
            "CREATE INDEX IF NOT EXISTS orderproduct_prod_sku_index ON OrderProduct (prod_sku)",
            "CREATE INDEX IF NOT EXISTS orderproduct_prod_ord_id_index ON OrderProduct (ord_id)",
            "CREATE INDEX IF NOT EXISTS ordertaxes_ord_id_index ON OrderProduct (ord_id)",
            "CREATE INDEX IF NOT EXISTS orders_cust_id_index ON Orders (cust_id)",
            "CREATE INDEX IF NOT EXISTS OrderProductsAttr_ordprod_id_index ON OrderProduct (ordprod_id)",
            "CREATE INDEX IF NOT EXISTS Payments_paymethod_id_index ON Payments (paymethod_id)",
            "CREATE INDEX IF NOT EXISTS Product_addons_prod_id_index ON Product_addons (prod_id)",
            "CREATE INDEX IF NOT EXISTS Taxes_tax_id_index ON Taxes (tax_id)",
            "CREATE INDEX IF NOT EXISTS Taxes_tax_code_id_index ON Taxes (tax_code_id)",
            "CREATE INDEX IF NOT EXISTS Taxes_Group_taxGroupId_index ON Taxes_Group (taxGroupId)",
            "CREATE INDEX IF NOT EXISTS Taxes_Group_taxId_index ON Taxes_Group (taxId)",
            "CREATE INDEX IF NOT EXISTS Taxes_Group_taxcode_id_index ON Taxes_Group (taxcode_id)",

            "CREATE INDEX IF NOT EXISTS VolumePrices_prod_id_index ON VolumePrices (prod_id)",
            "CREATE INDEX IF NOT EXISTS minQty_index ON VolumePrices (minQty)",
            "CREATE INDEX IF NOT EXISTS maxQty_index ON VolumePrices (maxQty)",
            "CREATE INDEX IF NOT EXISTS pricelevel_id_index ON VolumePrices (pricelevel_id)",
            "CREATE INDEX IF NOT EXISTS Products_Images_prod_id_index ON Products_Images (prod_id)",
            "CREATE INDEX IF NOT EXISTS type_index ON Products_Images (type)",
            "CREATE INDEX IF NOT EXISTS prod_sku_index ON Products (prod_sku)",
            "CREATE INDEX IF NOT EXISTS prod_upc_index ON Products (prod_upc)",
            "CREATE INDEX IF NOT EXISTS prod_name_index ON Products (prod_name)",
            "CREATE INDEX IF NOT EXISTS prod_type_index ON Products (prod_type)",
            "CREATE INDEX IF NOT EXISTS prod_taxcode_index ON Products (prod_taxcode)",
            "CREATE INDEX IF NOT EXISTS productchainxref_prod_id_index ON ProductChainXRef (prod_id)",
            "CREATE INDEX IF NOT EXISTS cust_chain_index ON ProductChainXRef (cust_chain)",
            "CREATE INDEX IF NOT EXISTS productaliases_alias_index ON ProductAliases (prod_alias)",
            "CREATE INDEX IF NOT EXISTS productaliases_prod_id_index ON ProductAliases (prod_id)",
            "CREATE INDEX IF NOT EXISTS pricelevelitems_prod_id_index ON PriceLevelItems (pricelevel_prod_id)",
            "CREATE INDEX IF NOT EXISTS pricepevel_pricelevel_id_index ON PriceLevel (pricelevel_id)",
            "CREATE INDEX IF NOT EXISTS salestaxcodes_taxcode_id_index ON SalesTaxCodes (taxcode_id)"
    };
    private final String CREATE_ADDRESS = "CREATE TABLE [Address] ([addr_id] varchar NOT NULL ,[cust_id]varchar NOT NULL ,[addr_b_str1]varchar,"
            + "[addr_b_str2]varchar,[addr_b_str3]varchar,[addr_b_city]varchar,[addr_b_state]varchar,[addr_b_country]varchar,[addr_b_zipcode]varchar,"
            + "[addr_s_name]varchar,[addr_s_str1]varchar,[addr_s_str2]varchar,[addr_s_str3]varchar,[addr_s_city]varchar,[addr_s_state]varchar,"
            + "[addr_s_country]varchar,[addr_s_zipcode]varchar,[qb_cust_id]varchar, [addr_b_type]VARCHAR, [addr_s_type]VARCHAR, PRIMARY KEY ([addr_id],"
            + "[cust_id]) )";
    private final String CREATE_SALES_ASSOCIATE = "CREATE TABLE [Clerk]([emp_id] [int] PRIMARY KEY NOT NULL,"
            + "[zone_id] [varchar](50),[emp_name][varchar](50),[emp_init] [varchar](50),[emp_pcs] [varchar](50)," +
            "[emp_lastlogin] [datetime],[emp_pos][int],[qb_emp_id] [varchar](50),[qb_salesrep_id] [varchar](50)," +
            "[isactive] [int],[tax_default][varchar](50),[loc_items] [tinyint]NOT NULL,[_rowversion][varchar](50)," +
            "[lastSync] [datetime],[TupyWalletDevice] [tinyint]NOT NULL,[VAT] [tinyint]NOT NULL" +
            ")";
    private final String CREATE_CATEGORIES = "CREATE TABLE [Categories]([cat_id] [varchar](50) PRIMARY KEY NOT NULL,"
            + "[cat_name] [varchar](255) NOT NULL,[cat_update] [datetime] NOT NULL,[isactive] "
            + "[tinyint] NOT NULL,[parentID] [varchar](50) NULL, [url_icon] [varchar])";
    private final String CREATE_CUSTOMERS = "CREATE TABLE [Customers]([cust_id] [varchar](50) PRIMARY KEY NOT NULL,[cust_id_ref] [varchar](50) NULL,"
            + "[qb_sync] [tinyint] NULL,[zone_id] [varchar](50) NULL,[CompanyName] [varchar](41) NULL,[Salutation] [varchar](15) NULL,"
            + "[cust_name] [varchar](255) NULL,[cust_chain] [int] NULL,[cust_balance] [money] NULL,[cust_limit] [money] NULL,"
            + "[cust_contact] [varchar](255) NULL,[cust_firstName] [varchar](25) NULL,[cust_middleName] [varchar](5) NULL,"
            + "[cust_lastName] [varchar](25) NULL,[cust_phone] [varchar](21) NULL,[cust_email] [varchar](1023) NULL,[cust_fax] [varchar](21) NULL,"
            + "[cust_update] [datetime] NULL,[isactive] [tinyint] NULL,[cust_ordertype] [int] NULL,[cust_taxable] [varchar](50) NULL,"
            + "[cust_salestaxcode] [varchar](50) NULL,[pricelevel_id] [varchar](50) NULL,[cust_terms] [varchar](50) NULL,[cust_pwd] [varchar](50) NULL,"
            + "[cust_securityquestion] [varchar](150) NULL,[cust_securityanswer] [varchar](50) NULL,[cust_points] [int] NULL, [custidkey] VARCHAR, "
            + "[cust_id_numeric] VARCHAR,[AccountNumnber] [varchar], [cust_dob] DATETIME)";
    private final String CREATE_DRAWDATEINFO = "CREATE TABLE DrawDateInfo (ID [varchar],CalendarVersionID [varchar], DrawNumber [varchar], "
            + "DrawDate [varchar], CutOffDate [varchar],CutOffTime[varchar],CutOffDateTime [datetime],OpportunityFactor [varchar] )";
    private final String CREATE_EMPINV = "CREATE TABLE [EmpInv]( [emp_inv_id] [int] PRIMARY KEY NOT NULL, [emp_id] [int] NULL, "
            + "[loc_id] [varchar],[prod_id] [varchar](50) NULL, [prod_onhand] [int] NULL, [emp_update] [datetime] NULL, [issync] [int] DEFAULT 0)";
    private final String CREATE_EMPLOYEES = "CREATE TABLE [Employees]( [emp_id] [int] PRIMARY KEY NOT NULL, [zone_id] [varchar](50) NULL, "
            + "[emp_name] [varchar](50) NULL, [emp_init] [varchar](50) NULL, [emp_pcs] [varchar](50) NULL, [emp_carrier] [int] NULL, "
            + "[emp_lastlogin] [smalldatetime] NULL, [emp_cleanup] [int] NULL, [emp_pos] [int] NULL, [qb_emp_id] [varchar](50) NULL, "
            + "[qb_salesrep_id] [varchar](50) NULL, [quota_month_goal] [money] NULL, [quota_month] [money] NULL, [quota_year_goal] [money] NULL, "
            + "[quota_year] [money] NULL, [emp_pwd] [nvarchar](50) NULL, [isactive] [tinyint] NULL, [email] [varchar](255) NULL, "
            + "[classid] [varchar](50) NULL, [tax_default] [varchar](50) NULL, [pricelevel_id] [varchar](50) NULL)";
    private final String CREATE_INVPRODUCTS = "CREATE TABLE [InvProducts]( [ordprod_id] [varchar](255) PRIMARY KEY NOT NULL, "
            + "[prod_id] [varchar](50) NOT NULL, [ord_id] [varchar](50) NOT NULL, [ordprod_qty] [real] NOT NULL, [overwrite_price] [money] NOT NULL, "
            + "[reason_id] [int] NULL, [ordprod_desc] [varchar](4095) NULL, [pricelevel_id] [varchar](50) NULL, [prod_seq] [int] NULL, "
            + "[uom_name] [varchar](50) NULL, [uom_conversion] [real] NULL)";
    private final String CREATE_INVOICEPAYMENTS = "CREATE TABLE InvoicePayments (pay_id [varchar],inv_id [varchar],applied_amount [double], [txnID] VARCHAR)";
    private final String CREATE_INVOICES = "CREATE TABLE [Invoices]( [inv_id] [varchar](50) PRIMARY KEY NOT NULL, [cust_id] [varchar](50) NULL, "
            + "[emp_id] [int] NULL, [inv_timecreated] [datetime] NULL, [inv_ispending] [int] NULL, [inv_ponumber] [varchar](50) NULL, "
            + "[inv_terms] [varchar](2000) NULL, [inv_duedate] [datetime] NULL, [inv_shipdate] [datetime] NULL, [inv_shipmethod] [varchar](255) NULL, "
            + "[inv_total] [money] NULL, [inv_apptotal] [money] NULL, [inv_balance] [money] NULL, [inv_custmsg] [varchar](2000) NULL, "
            + "[inv_ispaid] [int] NULL, [inv_paiddate] [datetime] NULL, [mod_date] [datetime] NULL, [txnID] [varchar](255) NULL, "
            + "[inv_update] [datetime] NULL)";
    private final String CREATE_ORDERPRODUCTS = "CREATE TABLE [OrderProduct]( [ordprod_id] [uniqueidentifier] PRIMARY KEY NOT NULL, "
            + "[addon_ordprod_id] [varchar](50), [prod_id] [varchar](50) NOT NULL, [ord_id] [varchar](50) NOT NULL, [ordprod_qty] [real] NOT NULL, [overwrite_price] [money] NULL, "
            + "[reason_id] [int] NULL, [ordprod_desc] [varchar](4095) NULL, [pricelevel_id] [varchar](50) NULL, [prod_seq] [int] NULL, "
            + "[uom_name] [varchar](50) NULL, [uom_conversion] [real] NULL, [discount_id] [varchar](50) NULL, [discount_value] [money] NULL, "
            + "[item_void] [tinyint] NULL, [isPrinted] [bit] NULL, [cat_id] [varchar](50) NULL, [cat_name] [varchar](50) NULL, [addon] [bit] NULL, "
            + "[isAdded] [bit] NULL, [ordprod_name][varchar](50) NULL, [prod_taxId][varchar](50) NULL, [prod_taxValue][money] NULL, "
            + "[uom_id] [varchar],[prod_istaxable][tinyint] NULL,[discount_is_taxable][tinyint],[discount_is_fixed][tinyint],[onHand][double],"
            + "[imgURL][varchar],[prod_price][money],[prod_type][varchar],[cardIsActivated][tinyint] DEFAULT 0,[itemTotal][money],[itemSubtotal][money],[addon_section_name][varchar],"
            + "[addon_position][varchar],[hasAddons][tinyint] DEFAULT 0,[ordprod_comment][varchar](50),[prod_sku] [varchar](255) NULL, " +
            " [isGC] [bit] NULL, [prod_upc] [varchar](50) NULL, [assignedSeat] [varchar](10), [seatGroupId][int] NULL, " +
            " [product_taxes_json][varchar], [prod_price_points] [int] NULL)";
    private final String CREATE_ORDERS = "CREATE TABLE [Orders]( [ord_id] [varchar](50) PRIMARY KEY NOT NULL, [qbord_id] [varchar](50) NULL, "
            + "[qbtxid] [varchar](255) NULL, [emp_id] [int] NULL, [cust_id] [varchar](50) NULL,[custidkey] [varchar], [ord_po] [varchar](50) NULL, [total_lines] [int] NULL, "
            + "[total_lines_pay] [int] NULL, [ord_total] [money] NULL, [ord_signature] [image] NULL, [ord_comment] [varchar](255) NULL, "
            + "[ord_delivery] [datetime] NULL, [ord_timecreated] [datetime] NULL, [ord_timesync] [datetime] NULL, [qb_synctime] [datetime] NULL, "
            + "[emailed] [int] NULL, [processed] [int] NULL, [ord_type] [int] NULL, [ord_claimnumber] [varchar](50) NULL, "
            + "[ord_rganumber] [varchar](50) NULL, [ord_returns_pu] [tinyint] NULL, [ord_inventory] [tinyint] NULL, [ord_issync] [tinyint] DEFAULT 0, "
            + "[tax_id] [varchar](50) NULL, [ord_shipvia] [varchar](50) NULL, [ord_shipto] [varchar](50) NULL, [ord_terms] [varchar](50) NULL, "
            + "[ord_custmsg] [varchar](50) NULL, [ord_class] [varchar](50) NULL, [ord_subtotal] [money] NULL, [ord_taxamount] [money] NULL, "
            + "[ord_discount] [money] NULL, [user_ID] [int] NULL, [addr_b_str1] [varchar](41) NULL, [addr_b_str2] [varchar](41) NULL, "
            + "[addr_b_str3] [varchar](41) NULL, [addr_b_city] [varchar](31) NULL, [addr_b_state] [varchar](21) NULL, "
            + "[addr_b_country] [varchar](31) NULL, [addr_b_zipcode] [varchar](13) NULL, [addr_s_str1] [varchar](41) NULL, "
            + "[addr_s_str2] [varchar](41) NULL, [addr_s_str3] [varchar](41) NULL, [addr_s_city] [varchar](31) NULL, [addr_s_state] [varchar](21) NULL, "
            + "[addr_s_country] [varchar](31) NULL, [addr_s_zipcode] [varchar](13) NULL, [c_email] [varchar](100) NULL, [loc_id] [varchar](50) NULL, "
            + "[ord_HoldName] [varchar](50) NULL,[isOnHold] [tinyint] NULL, [clerk_id][varchar](50) NULL, [ord_discount_id][varchar](50) NULL, [ord_latitude][varchar](50) NULL, "
            + "[ord_longitude][varchar](50) NULL, [tipAmount][varchar](50) NULL , isVoid tinyint, [is_stored_fwd] BOOL DEFAULT (0), VAT tinyint," +
            " [bixolonTransactionId] [varchar](50) NULL, [assignedTable] [varchar](10) NULL, [numberOfSeats] [int] NULL, associateID [varchar](10) NULL, [ord_timeStarted] [datetime] NULL,[orderAttributes] [varchar](1000) NULL)";
    private final String CREATE_PAYMETHODS = "CREATE TABLE [PayMethods]( [paymethod_id] [varchar](50) PRIMARY KEY NOT NULL, "
            + "[paymethod_name] [varchar](255) NOT NULL, [paymentmethod_type] [varchar](50) NULL, [paymethod_update] [datetime] NOT NULL, "
            + "[isactive] [tinyint] NOT NULL, [paymethod_showOnline] [tinyint] NULL, [image_url] [varchar] NULL, [OriginalTransid] [BOOL] DEFAULT (0))";
    private final String CREATE_PAYMENTS = "CREATE TABLE [Payments] ([pay_id] varchar PRIMARY KEY  NOT NULL ,[group_pay_id] varchar,[cust_id] varchar,"
            + "[emp_id] int,[custidkey] [varchar],[tupyx_user_id][varchar](50),[inv_id] varchar,[paymethod_id] varchar,[pay_check] varchar,[pay_receipt] varchar,[pay_amount] money,[pay_comment] varchar,"
            + "[pay_dueamount][money],[pay_timecreated] datetime,[pay_timesync] datetime,[account_id] varchar,[processed] int," +
            " [pay_signature_issync] tinyint DEFAULT 0, [pay_issync] tinyint DEFAULT 0,[pay_transid] varchar,"
            + "[pay_refnum] varchar,[pay_name] varchar,[pay_addr] varchar,[pay_poscode] varchar,[pay_seccode] varchar,[pay_maccount] varchar,"
            + "[pay_groupcode] varchar,[pay_stamp] varchar,[pay_resultcode] varchar,[pay_resultmessage] varchar,[pay_ccnum] varchar,"
            + "[pay_expmonth] varchar,[pay_expyear] varchar,[pay_expdate] varchar,[pay_result] varchar,[pay_date] datetime,[recordnumber] varchar,"
            + "[pay_signature] varchar,[authcode] varchar,[status] int,[job_id] varchar,[user_ID] int,[pay_type] int,[pay_tip] money,"
            + "[ccnum_last4] varchar,[pay_phone] varchar,[pay_email] varchar,[isVoid] tinyint,[tipAmount] varchar,[clerk_id] varchar,"
            + "[pay_latitude] varchar,[pay_longitude] varchar,[is_refund] BOOL DEFAULT (0) ,[ref_num] varchar,[IvuLottoNumber] VARCHAR,"
            + "[IvuLottoDrawDate] VARCHAR,[IvuLottoQR] VARCHAR,[showPayment][tinyint] DEFAULT(1),[original_pay_id][varchar],[card_type] VARCHAR,[Tax1_amount] VARCHAR,[Tax1_name] VARCHAR,[Tax2_amount] VARCHAR,"
            + "[Tax2_name] VARCHAR, [EMV_JSON] VARCHAR, [amount_tender] money)";
    private final String CREATE_PAYMENTS_DECLINED = "CREATE TABLE [PaymentsDeclined] ([pay_id] varchar PRIMARY KEY  NOT NULL ,[group_pay_id] varchar,[cust_id] varchar,"
            + "[emp_id] int,[custidkey] [varchar],[tupyx_user_id][varchar](50),[inv_id] varchar,[paymethod_id] varchar,[pay_check] varchar,[pay_receipt] varchar,[pay_amount] money,[pay_comment] varchar,"
            + "[pay_dueamount][money],[pay_timecreated] datetime,[pay_timesync] datetime,[account_id] varchar,[processed] int,[pay_issync] tinyint DEFAULT 0,[pay_transid] varchar,"
            + "[pay_refnum] varchar,[pay_name] varchar,[pay_addr] varchar,[pay_poscode] varchar,[pay_seccode] varchar,[pay_maccount] varchar,"
            + "[pay_groupcode] varchar,[pay_stamp] varchar,[pay_resultcode] varchar,[pay_resultmessage] varchar,[pay_ccnum] varchar,"
            + "[pay_expmonth] varchar,[pay_expyear] varchar,[pay_expdate] varchar,[pay_result] varchar,[pay_date] datetime,[recordnumber] varchar,"
            + "[pay_signature] varchar,[authcode] varchar,[status] int,[job_id] varchar,[user_ID] int,[pay_type] int,[pay_tip] money,"
            + "[ccnum_last4] varchar,[pay_phone] varchar,[pay_email] varchar,[isVoid] tinyint,[tipAmount] varchar,[clerk_id] varchar,"
            + "[pay_latitude] varchar,[pay_longitude] varchar,[is_refund] BOOL DEFAULT (0) ,[ref_num] varchar,[IvuLottoNumber] VARCHAR,"
            + "[IvuLottoDrawDate] VARCHAR,[IvuLottoQR] VARCHAR,[showPayment][tinyint] DEFAULT(1),[original_pay_id][varchar],[card_type] VARCHAR,[Tax1_amount] VARCHAR,[Tax1_name] VARCHAR,[Tax2_amount] VARCHAR,"
            + "[Tax2_name] VARCHAR, [EMV_JSON] VARCHAR, [amount_tender] money)";
    private final String CREATE_STORED_PAYMENTS = "CREATE TABLE [StoredPayments] (pay_uuid varchar PRIMARY KEY NOT NULL, [pay_id] varchar (50) ,[group_pay_id] varchar,[cust_id] varchar,"
            + "[emp_id] int,[custidkey] [varchar],[tupyx_user_id][varchar](50),[inv_id] varchar,[paymethod_id] varchar,[pay_check] varchar,[pay_receipt] varchar,[pay_amount] money,[pay_comment] varchar,"
            + "[pay_dueamount][money],[pay_timecreated] datetime,[pay_timesync] datetime,[account_id] varchar,[processed] int,[pay_issync] tinyint DEFAULT 0,[pay_transid] varchar,"
            + "[pay_refnum] varchar,[pay_name] varchar,[pay_addr] varchar,[pay_poscode] varchar,[pay_seccode] varchar,[pay_maccount] varchar,"
            + "[pay_groupcode] varchar,[pay_stamp] varchar,[pay_resultcode] varchar,[pay_resultmessage] varchar,[pay_ccnum] varchar,"
            + "[pay_expmonth] varchar,[pay_expyear] varchar,[pay_expdate] varchar,[pay_result] varchar,[pay_date] datetime,[recordnumber] varchar,"
            + "[pay_signature] varchar,[authcode] varchar,[status] int,[job_id] varchar,[user_ID] int,[pay_type] int,[pay_tip] money,"
            + "[ccnum_last4] varchar,[pay_phone] varchar,[pay_email] varchar,[isVoid] tinyint,[tipAmount] varchar,[clerk_id] varchar,"
            + "[pay_latitude] varchar,[pay_longitude] varchar,[is_refund] BOOL DEFAULT (0) ,[ref_num] varchar,[IvuLottoNumber] VARCHAR,"
            + "[IvuLottoDrawDate] VARCHAR,[IvuLottoQR] VARCHAR,[showPayment][tinyint] DEFAULT(1),[original_pay_id][varchar],[card_type] VARCHAR,[Tax1_amount] VARCHAR,[Tax1_name] VARCHAR,[Tax2_amount] VARCHAR,"
            + "[Tax2_name] VARCHAR, payment_xml varchar, is_retry BOOL DEFAULT (0) , [EMV_JSON] VARCHAR, [amount_tender] money)";
    private final String CREATE_PRICELEVEL = "CREATE TABLE [PriceLevel]( [pricelevel_id] [varchar](50) PRIMARY KEY NOT NULL, [pricelevel_name] [varchar](50) NULL, "
            + "[pricelevel_type] [varchar](50) NULL, [pricelevel_fixedpct] [float] NULL, [pricelevel_update] [datetime] NOT NULL, [isactive] [tinyint] NOT NULL)";
    private final String CREATE_PRICELEVELITEMS = "CREATE TABLE [PriceLevelItems]( [pricelevel_prod_id] [varchar](50) NOT NULL, "
            + "[pricelevel_id] [varchar](50)  NOT NULL, [pricelevel] [varchar](50) NULL, [pricelevel_price] [money] NOT NULL, "
            + "[pricelevel_update] [datetime] NOT NULL, [isactive] [tinyint] NOT NULL, PRIMARY KEY(pricelevel_prod_id,pricelevel_id))";
    private final String CREATE_PRINTERS = "CREATE TABLE [Printers]( [print_id] integer PRIMARY KEY autoincrement NOT NULL,[printer_id] [int] NOT NULL, [printer_name] [varchar](255) NOT NULL, "
            + "[printer_ip] [varchar](255) NOT NULL, [printer_port] [varchar](10) NULL, [printer_type] [varchar](50) NULL,[cat_name][varchar](50) NULL,"
            + "[cat_id][varchar](50) NULL)";
    private final String CREATE_PRINTERS_LOCATIONS = "CREATE TABLE [Printers_Locations]( [printerloc_key] [int] PRIMARY KEY NOT NULL, "
            + "[loc_id] [varchar](50) NOT NULL, [cat_id] [varchar](50) NOT NULL, [printer_id] [int] NOT NULL)";
    private final String CREATE_PRODCATXREF = "CREATE TABLE [ProdCatXref]( [idKey] [int] PRIMARY KEY NOT NULL, [prod_id] [varchar](50) NOT NULL, "
            + "[cat_id] [varchar](50) NOT NULL, [_update] [datetime] NULL, [isactive] [bit] NOT NULL)";
    private final String CREATE_PRODUCTCHAINXREF = "CREATE TABLE [ProductChainXRef]( [chainKey] [uniqueidentifier] PRIMARY KEY NOT NULL, "
            + "[cust_chain] [varchar](50) NOT NULL, [prod_id] [varchar](50) NOT NULL, [over_price_gross] [money] NULL, [over_price_net] [money] NOT NULL, "
            + "[isactive] [tinyint] NOT NULL, [productchain_update] [datetime] NULL, [customer_item] [varchar](20) NULL)";
    private final String CREATE_PRODUCT_ADDONS = "CREATE TABLE [Product_addons]( [rest_addons] [int] PRIMARY KEY NOT NULL, [prod_id] [varchar](50) NULL, "
            + "[cat_id] [varchar](50) NULL, [isactive] [bit] NULL, [_update] [datetime] NULL)";
    private final String CREATE_PRODUCTS = "CREATE TABLE [Products]( [prod_id] [varchar](50) PRIMARY KEY NOT NULL, [prod_type] [varchar](50) NULL, "
            + "[prod_disc_type] [varchar](50) NULL, [cat_id] [varchar](50) NULL, [prod_sku] [varchar](255) NULL, [prod_upc] [varchar](50) NULL, "
            + "[prod_name] [varchar](255) NULL, [prod_desc] [varchar](4000) NULL, [prod_extradesc] [varchar](255) NULL, [prod_onhand] [real] NULL, "
            + "[prod_onorder] [real] NULL, [prod_uom] [varchar](50) NULL, [prod_price] [money] NULL, [prod_cost] [money] NULL, "
            + "[prod_taxcode] [varchar](50) NULL, [prod_taxtype] [varchar](50) NULL, [prod_glaccount] [varchar](50) NULL, [prod_mininv] [int] NULL, "
            + "[prod_update] [datetime] NULL, [isactive] [int] NULL, [prod_showOnline] [tinyint] NULL, [prod_ispromo] [tinyint] NULL, "
            + "[prod_shipping] [tinyint] NULL, [prod_weight] [real] NULL, [prod_expense] [bit] NULL, [prod_disc_type_points] [varchar](255) NULL, "
            + "[isGC] [bit] NULL, [prod_price_points] [int] NULL, [prod_value_points] [int] NULL, [prod_prices_group_id] [varchar](50) NULL)";
    private final String CREATE_PRODUCTALIASES = "CREATE TABLE [ProductAliases] ([alias_id_pk] INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "[prod_id] [varchar](50), [prod_alias] [varchar](50))";
    private final String CREATE_PRODUCTS_IMAGES = "CREATE TABLE [Products_Images]( [img_id] [int] PRIMARY KEY NOT NULL, "
            + "[prod_id] [varchar](50) NOT NULL, [prod_img_name] [varchar](255) NOT NULL, [prod_default] [tinyint] NOT NULL, [type] [nchar](1) NULL)";
    private final String CREATE_PUBLICVARIABLES = "CREATE TABLE [PublicVariables]( [MSEmployeeID] [int] PRIMARY KEY NOT NULL, "
            + "[MSEmployeeName] [nvarchar](50) NULL, [MSDeviceID] [nvarchar](255) NULL, [MSZoneID] [nvarchar](50) NULL, [MSLastSynch] [datetime] NULL, "
            + "[MSOrderLastSynch] [datetime] NULL, [MSActivationKey] [nvarchar](27) NULL, [MSConnection] [nvarchar](50) NULL, "
            + "[MSTicket] [nvarchar](50) NULL, [MSRegID] [nvarchar](50) NULL, [MSAccount] [nvarchar](50) NULL, [MSUser] [nvarchar](50) NULL, "
            + "[MSPass] [nvarchar](50) NULL, [MPUser] [nvarchar](50) NULL, [MPPass] [nvarchar](50) NULL, [MSQBMS] [int] NULL, "
            + "[MSLastOrderID] [nvarchar](50) NULL, [MSOrderEntry] [nvarchar](12) NULL, [MSOrderType] [nvarchar](12) NULL, "
            + "[MSCardProcessor] [nvarchar](1) NULL, [MSPrinter] [int] NULL, [MSLanguage] [nvarchar](50) NULL)";
    private final String CREATE_REASONS = "CREATE TABLE [Reasons]( [reason_id] [int] PRIMARY KEY NOT NULL, [reason_name] [varchar](50) NULL, "
            + "[reason_date] [datetime] NULL, [isactive] [tinyint] NULL)";
    private final String CREATE_REFUNDS = "CREATE TABLE Refunds ( pk INTEGER PRIMARY KEY, ent INTEGER, opt INTEGER, card_exp_month INTEGER, "
            + "card_exp_year INTEGER, is_signa_sent INTEGER, issync tinyint DEFAULT 0, processed INTEGER, swipe INTEGER, pay_date TIMESTAMP, "
            + "time_created TIMESTAMP, time_sync TIMESTAMP, amount DECIMAL, uid VARCHAR, address VARCHAR, app_id VARCHAR, auth_code VARCHAR, "
            + "card_number VARCHAR, card_sec_code VARCHAR,check_number VARCHAR,comment VARCHAR, cust_email VARCHAR, cust_id VARCHAR, emp_name VARCHAR,"
            + "emp_id VARCHAR, group_code VARCHAR, group_payid VARCHAR, inv_id VARCHAR, inventory_id VARCHAR, isVoid VARCHAR,job_id VARCHAR,"
            + "latitude VARCHAR, longitude VARCHAR, merch_acct VARCHAR, paymethod_id VARCHAR,paymethod_name VARCHAR, receipt VARCHAR, "
            + "record_number VARCHAR, ref_number VARCHAR,result VARCHAR, result_code VARCHAR, result_msg VARCHAR, signature VARCHAR, stamp VARCHAR, "
            + "status VARCHAR,track2 VARCHAR, trans_id VARCHAR, usr_id VARCHAR, zip_code VARCHAR, grouped_tax_dict BLOB, thumb_img BLOB )";
    private final String CREATE_SALESTAXCODES = "CREATE TABLE [SalesTaxCodes]( [taxcode_id] [varchar](50) PRIMARY KEY NOT NULL, "
            + "[taxcode_name] [varchar](255) NOT NULL, [taxcode_desc] [varchar](255) NOT NULL, [taxcode_istaxable] [int] NOT NULL, "
            + "[isactive] [tinyint] NOT NULL, [taxcode_update] [datetime] NOT NULL)";
    private final String CREATE_SHIPMETHOD = "CREATE TABLE [ShipMethod]( [shipmethod_id] [varchar](50) PRIMARY KEY NOT NULL, "
            + "[shipmethod_name] [varchar](255) NOT NULL, [isactive] [tinyint] NOT NULL, [shipmethod_update] [datetime] NOT NULL)";
    private final String CREATE_TAXES = "CREATE TABLE [Taxes]( [tax_id_key] PRIMARY KEY NOT NULL, [tax_id] [varchar](50) NOT NULL, "
            + "[tax_name] [varchar](50) NOT NULL, [tax_code_id] [varchar](50) NULL, [tax_code_name] [varchar](50) NULL, [tax_rate] [float] NOT NULL, "
            + "[tax_type] [char](1) NULL, [isactive] [tinyint] NOT NULL, [tax_update] [datetime] NOT NULL, [prTax] [varchar](4) NULL, "
            + "[tax_default] [tinyint] NULL, [tax_account] [varchar](50) NULL)";
    private final String CREATE_TAXES_GROUP = "CREATE TABLE [Taxes_Group]( [taxGroupKey] [int] PRIMARY KEY NOT NULL, "
            + "[taxGroupId] [varchar](50) NOT NULL, [taxId] [varchar](50) NOT NULL, [taxcode_id] [varchar](50) NULL, [tax_rate] [nchar](10) NULL, "
            + "[taxLowRange] [money] NULL, [taxHighRange] [money] NULL, [taxgroup_update] [datetime] NULL, [isactive] [tinyint] NULL)";
    private final String CREATE_TEMPLATES = "CREATE TABLE Templates (_id [varchar],cust_id [varchar],product_id[varchar],quantity [double],"
            + "price_level_id [varchar],price_level [varchar], name [varchar], price [money], overwrite_price [money], _update[date],"
            + "isactive [varchar],isSync[boolean] DEFAULT 0,[prod_sku] [varchar](255) NULL, [prod_upc] [varchar](50) NULL)";
    private final String CREATE_TERMS = "CREATE TABLE [Terms]( [terms_id] [varchar](50) PRIMARY KEY NOT NULL, [terms_name] [varchar](255) NOT NULL, "
            + "[terms_stdduedays] [int] NULL, [terms_stddiscdays] [int] NULL, [terms_discpct] [float] NULL, [isactive] [tinyint] NOT NULL, "
            + "[terms_update] [datetime] NOT NULL)";
    private final String CREATE_UOM = "CREATE TABLE [UOM]( [uomitem_id] [varchar](50) NOT NULL, [uom_id] [varchar](50) NOT NULL, "
            + "[uom_name] [varchar](50) NOT NULL, [prod_id] [varchar](50) NOT NULL, [uom_conversion] [float] NULL, [uom_update] [datetime] NULL, "
            + "[isactive] [int] NULL)";
    private final String CREATE_VOIDTRANSACTIONS = "CREATE TABLE [VoidTransactions] ([ord_id] varchar(100) NOT NULL ,[ord_type] varchar(50) NOT NULL ,"
            + "[processed] int,[ord_timesync] datetime,[qb_synctime] datetime,[is_sync] int NOT NULL  DEFAULT 1)";
    private final String CREATE_VOLUMEPRICES = "CREATE TABLE [VolumePrices]( [id_key] [varchar](50) PRIMARY KEY NOT NULL, [prod_id] [varchar](50) NULL, "
            + "[minQty] [real] NOT NULL, [maxQty] [real] NOT NULL, [price] [money] NOT NULL, [isactive] [varchar](50) NULL, [pricelevel_id] VARCHAR)";
    private final String CREATE_DEVICEDEFAULTVALUES = "CREATE TABLE [deviceDefaultValues]( [df_id] [varchar] PRIMARY KEY NOT NULL, "
            + "[posAdminPassword] [varchar](50) NULL, [loyaltyPointFeature][varchar](50) NULL, [pointsType] [varchar](50) NULL, "
            + "[defaultPointsPricePercentage] [varchar](50) NULL, [globalDiscountID] [varchar](50) NULL)";
    private final String CREATE_MEMOTEXT = "CREATE TABLE [memotext]( [memo_id] [int] PRIMARY KEY NOT NULL, [memo_headerLine1] [varchar](100) NULL, "
            + "[memo_headerLine2] [varchar](100) NULL, [memo_headerLine3] [varchar](100) NULL, [memo_footerLine1] [varchar](100) NULL, "
            + "[memo_footerLine2] [varchar](100) NULL, [memo_footerLine3] [varchar](100) NULL, [store_name] [varchar](100) NULL, "
            + "[store_email] [varchar](100) NULL, [isactive] [tinyint] NULL)";
    private final String CREATE_PRODUCTS_ATTRS = "CREATE TABLE [products_attrs]( [prodAttrKey] [int] PRIMARY KEY NOT NULL, "
            + "[prod_id] [varchar](50) NOT NULL, [attr_id] [varchar](50) NOT NULL, [attr_name] [varchar](50) NULL, [attr_desc] [varchar](50) NULL, "
            + "[attr_group] [varchar](50) NULL, [attr_group_id] [varchar](50) NULL)";
    private final String CREATE_CONSIGNMENT_TRANSACTION = "CREATE TABLE [ConsignmentTransaction] ([Cons_ID] integer PRIMARY KEY autoincrement NOT NULL  DEFAULT (0) ,"
            + "[ConsTrans_ID]varchar,[ConsEmp_ID]varchar,[ConsCust_ID]varchar,[ConsInvoice_ID]varchar,[ConsPickup_ID]varchar,[ConsReturn_ID]varchar,"
            + "[ConsDispatch_ID]varchar,[ConsProd_ID]varchar,[ConsOriginal_Qty] real,[ConsStock_Qty] real,[ConsInvoice_Qty]real,[ConsReturn_Qty]real,"
            + "[ConsDispatch_Qty] real, [ConsNew_Qty] real,[ConsPickup_Qty] real,[Cons_timecreated] datetime, [is_synched] int NOT NULL  DEFAULT 1)";
    private final String CREATE_CUSTOMER_INVENTORY = "CREATE TABLE [CustomerInventory] ([consignment_id] integer PRIMARY KEY autoincrement NOT NULL DEFAULT (null) ,"
            + "[cust_id]varchar,[prod_id]varchar,[qty]int,[price]money,[prod_name] [varchar],[cust_update]datetime,[is_synched] int NOT NULL  DEFAULT 1)";
    private final String CREATE_CONSIGNMENT_SIGNATURES = "CREATE TABLE [ConsignmentSignatures] ([ConsTrans_ID] varchar PRIMARY KEY NOT NULL,[encoded_signature] varchar)";
    private final String CREATE_PRODUCTS_ATTRIBUTES = "CREATE TABLE [ProductsAttr] ([prodAttrKey] [int] PRIMARY KEY NOT NULL, [prod_id] [varchar](50) NOT NULL, "
            + "[attr_id] [varchar](50) NOT NULL, [attr_name] [varchar] (50) NULL, [attr_desc] [varchar](50) NULL, [attr_group][varchar](50) NULL,"
            + "[attr_group_id] [varchar](50) NULL)";
    private final String CREATE_TERMS_AND_CONDITIONS = "CREATE TABLE [TermsAndConditions] ([tc_id] [varchar](50) PRIMARY KEY NOT NULL,"
            + "[tc_term] [varchar] NOT NULL, [loc_id][varchar](50) NULL)";
    private final String CREATE_CLERKS = "CREATE TABLE [Clerks]( [emp_id] [int] PRIMARY KEY NOT NULL, [zone_id] [varchar](50) NULL, "
            + "[emp_name] [varchar](50) NULL, [emp_init] [varchar](50) NULL, [emp_pcs] [varchar](50) NULL, [emp_carrier] [int] NULL, "
            + "[emp_lastlogin] [smalldatetime] NULL, [emp_cleanup] [int] NULL, [emp_pos] [int] NULL, [qb_emp_id] [varchar](50) NULL, "
            + "[qb_salesrep_id] [varchar](50) NULL, [quota_month_goal] [money] NULL, [quota_month] [money] NULL, [quota_year_goal] [money] NULL, "
            + "[quota_year] [money] NULL, [emp_pwd] [nvarchar](50) NULL, [isactive] [tinyint] NULL, [email] [varchar](255) NULL, "
            + "[classid] [varchar](50) NULL, [tax_default] [varchar](50) NULL, [pricelevel_id] [varchar](50) NULL)";
    private final String CREATE_TIMECLOCK = "CREATE TABLE TimeClock(timeclockid varchar(50) PRIMARY KEY NOT NULL, emp_id integer NULL, status varchar(4) "
            + "NULL, punchtime datetime NULL, updated datetime NULL,issync tinyint DEFAULT 0)";
    private final String CREATE_SHIFTPERIODS = "CREATE TABLE [ShiftPeriods] ([shift_id] [varchar](50) PRIMARY KEY NOT NULL,[assignee_id][int],"
            + "[assignee_name][varchar](50),[creationDate][datetime],[creationDateLocal][datetime],[startTime][datetime],[startTimeLocal][datetime],"
            + "[endTime][datetime],[endTimeLocal][datetime],[beginning_petty_cash][decimal],[ending_petty_cash][decimal],[entered_close_amount][decimal],"
            + "[total_transaction_cash][decimal],[shift_issync] [int] DEFAULT 0)";
    private final String CREATE_EXPENSES = "CREATE TABLE [Expenses] ([expenseID] [varchar](50) PRIMARY KEY NOT NULL,[shiftPeriodID] [varchar](50) NOT NULL,[cashAmount][decimal],"
            + "[productID][varchar](50),[productName][varchar](255))";
    private final String CREATE_ORDER_PRODUCTS_ATTR = "CREATE TABLE OrderProductsAttr (ordprodattr_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "ordprod_id varchar(50), attribute_id varchar(50),name varchar,value varchar)";
    private final String CREATE_ORD_PROD_ATTR_LIST = "CREATE TABLE OrdProdAttrList (ordprodattr_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "Prod_id varchar(50), Attrid varchar(50),ordprod_attr_name varchar,required BOOL)";
    private final String CREATE_ORDER_TAXES = "CREATE TABLE OrderTaxes ([ord_tax_id] [varchar](50) PRIMARY KEY NOT NULL, [ord_id][varchar](50),"
            + "[tax_name][varchar](50),[tax_rate][money],[tax_amount][money])";
    private final String CREATE_LOCATIONS = "CREATE TABLE [Locations] ([loc_key] varchar (50) PRIMARY KEY NOT NULL, [loc_id] varchar (50), [loc_name] varchar (50))";
    private final String CREATE_LOCATIONS_INVENTORY = "CREATE TABLE [LocationsInventory] ([loc_id] varchar (50), [prod_id] varchar (50), [prod_onhand] real)";
    private final String CREATE_TRANSFER_LOCATIONS = "CREATE TABLE [TransferLocations] ([trans_id] varchar (50) PRIMARY KEY NOT NULL,"
            + " [loc_key_from] varchar (50), [loc_key_to] varchar (50), [emp_id]varchar(50), [trans_timecreated] datetime, [issync] [int] DEFAULT 0 )";
    private final String CREATE_TRANSFER_INVENTORY = "CREATE TABLE [TransferInventory] ([trans_key] integer PRIMARY KEY autoincrement NOT NULL DEFAULT(0),"
            + " [trans_id] varchar (50), [prod_id] varchar (50), [prod_qty] real)";
    private final String CREATE_PAYMENTS_XML = "CREATE TABLE [PaymentsXML]([app_id] [varchar](100) PRIMARY KEY NOT NULL, [payment_xml] [varchar] NOT NULL)";
    private final String[] TABLE_NAME = new String[]{"Address", "Categories", "Clerk", "Customers", "DrawDateInfo", "EmpInv",
            "Employees", "InvProducts", "InvoicePayments", "Invoices", "OrderProduct", "Orders", "PayMethods",
            "Payments", "PaymentsDeclined", "PriceLevel", "PriceLevelItems", "Printers", "Printers_Locations", "ProdCatXref",
            "ProductChainXRef", "Product_addons", "Products", "Products_Images", "PublicVariables", "Reasons",
            "Refunds", "SalesTaxCodes", "ShipMethod", "Taxes", "Taxes_Group", "Templates", "Terms", "UOM",
            "VoidTransactions", "VolumePrices", "deviceDefaultValues", "memotext", "products_attrs",
            "ConsignmentTransaction", "CustomerInventory", "ProductsAttr", "TermsAndConditions", "Clerks", "TimeClock",
            "ShiftPeriods", "ConsignmentSignatures", "OrderProductsAttr", "OrdProdAttrList", "ProductAliases",
            "OrderTaxes", "Locations", "LocationsInventory", "TransferLocations", "TransferInventory", "PaymentsXML",
            "StoredPayments", "Expenses"};
    private final String[] CREATE_TABLE = new String[]{CREATE_ADDRESS, CREATE_CATEGORIES, CREATE_SALES_ASSOCIATE, CREATE_CUSTOMERS,
            CREATE_DRAWDATEINFO, CREATE_EMPINV, CREATE_EMPLOYEES, CREATE_INVPRODUCTS, CREATE_INVOICEPAYMENTS,
            CREATE_INVOICES, CREATE_ORDERPRODUCTS, CREATE_ORDERS, CREATE_PAYMETHODS, CREATE_PAYMENTS,
            CREATE_PAYMENTS_DECLINED, CREATE_PRICELEVEL,
            CREATE_PRICELEVELITEMS, CREATE_PRINTERS, CREATE_PRINTERS_LOCATIONS, CREATE_PRODCATXREF,
            CREATE_PRODUCTCHAINXREF, CREATE_PRODUCT_ADDONS, CREATE_PRODUCTS, CREATE_PRODUCTS_IMAGES,
            CREATE_PUBLICVARIABLES, CREATE_REASONS, CREATE_REFUNDS, CREATE_SALESTAXCODES, CREATE_SHIPMETHOD,
            CREATE_TAXES, CREATE_TAXES_GROUP, CREATE_TEMPLATES, CREATE_TERMS, CREATE_UOM, CREATE_VOIDTRANSACTIONS,
            CREATE_VOLUMEPRICES, CREATE_DEVICEDEFAULTVALUES, CREATE_MEMOTEXT, CREATE_PRODUCTS_ATTRS,
            CREATE_CONSIGNMENT_TRANSACTION, CREATE_CUSTOMER_INVENTORY, CREATE_PRODUCTS_ATTRIBUTES,
            CREATE_TERMS_AND_CONDITIONS, CREATE_CLERKS, CREATE_TIMECLOCK, CREATE_SHIFTPERIODS,
            CREATE_CONSIGNMENT_SIGNATURES, CREATE_ORDER_PRODUCTS_ATTR, CREATE_ORD_PROD_ATTR_LIST, CREATE_PRODUCTALIASES,
            CREATE_ORDER_TAXES, CREATE_LOCATIONS, CREATE_LOCATIONS_INVENTORY, CREATE_TRANSFER_LOCATIONS,
            CREATE_TRANSFER_INVENTORY, CREATE_PAYMENTS_XML, CREATE_STORED_PAYMENTS, CREATE_EXPENSES};
    private Context context;
    private DBManager managerInstance;
    private DatabaseHelper DBHelper;
    private MyPreferences myPref;

    public DBManager(Context context) {

        this.context = context;
        myPref = new MyPreferences(context);
        managerInstance = this;
        if ((getDatabase() == null || !getDatabase().isOpen())) {
            SQLiteDatabase.loadLibs(context);
//		exportDBFile();
            dbMigration();
            this.DBHelper = new DatabaseHelper(this.context);
            InitializeSQLCipher();
        }

    }

    public DBManager(Context context, int type) {
        this.context = context;
        managerInstance = this;
        if (type == Global.FROM_REGISTRATION_ACTIVITY) {
            resetDatabase();
        }
        myPref = new MyPreferences(context);
        if ((getDatabase() == null || !getDatabase().isOpen())) {
            SQLiteDatabase.loadLibs(context);
//		exportDBFile();
            dbMigration();
            InitializeSQLCipher();
        }

    }

    public static SQLiteDatabase getDatabase() {
        return database;
    }

    public static void setDatabase(SQLiteDatabase database) {
        DBManager.database = database;
    }

    public static void encrypt(Context ctxt, String dbName, String passphrase) throws IOException {
        File originalFile = ctxt.getDatabasePath(dbName);
//        originalFile = new File(Environment.getExternalStorageDirectory() + "/emobilepos.sqlite");
        if (originalFile.exists()) {
            File newFile = File.createTempFile("sqlcipherutils", "tmp", ctxt.getCacheDir());

            SQLiteDatabase db;
            try {
                db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(), "", null,
                        SQLiteDatabase.OPEN_READWRITE);

                db.rawExecSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';", newFile.getAbsolutePath(),
                        passphrase));

                db.rawExecSQL("SELECT sqlcipher_export('encrypted')");
                db.rawExecSQL("DETACH DATABASE encrypted;");

                int version = db.getVersion();

                db.close();

                db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(), passphrase, null,
                        SQLiteDatabase.OPEN_READWRITE);

                db.setVersion(version);
                db.close();

                originalFile.delete();
                newFile.renameTo(ctxt.getDatabasePath(CIPHER_DB_NAME));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static void decrypt(Context ctxt, String dbName, String passphrase) throws IOException {
        File originalFile = ctxt.getDatabasePath(CIPHER_DB_NAME);

        if (originalFile.exists()) {
            File newFile = File.createTempFile("sqlcipherutils", "tmp", ctxt.getCacheDir());
            SQLiteDatabase db;
            try {
                db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(), passphrase, null,
                        SQLiteDatabase.OPEN_READWRITE);

                db.rawExecSQL(String.format("ATTACH DATABASE '%s' AS cleartext KEY '';", newFile.getAbsolutePath()));

                db.rawExecSQL("SELECT sqlcipher_export('cleartext')");
                db.rawExecSQL("DETACH DATABASE cleartext;");

                int version = db.getVersion();

                db.close();

                db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(), "", null,
                        SQLiteDatabase.OPEN_READWRITE);

                db.setVersion(version);
                db.close();
                File outFile = new File(Environment.getExternalStorageDirectory() + "/emobilepos.db");
                try {
                    FileUtils.copyFile(newFile, outFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                originalFile.delete();
//                newFile.renameTo(ctxt.getDatabasePath(CIPHER_DB_NAME));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String getPassword() {
        MessageDigest digester;
        String md5;
        String android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        try {
            digester = MessageDigest.getInstance("MD5");
            digester.update(android_id.getBytes());
            md5 = Base64.encode(digester.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return PASSWORD;
        }
        if (TextUtils.isEmpty(md5))
            return PASSWORD;
        return md5;
    }

    private void InitializeSQLCipher() {
        try {
            setDatabase(SQLiteDatabase.openDatabase(context.getDatabasePath(CIPHER_DB_NAME).getAbsolutePath(), getPassword(),
                    null, SQLiteDatabase.OPEN_READWRITE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean resetDatabase() {
        return context.deleteDatabase(CIPHER_DB_NAME);
    }

    private void dbMigration() {
        File dbPath = null;
        try {
            dbPath = context.getDatabasePath(DB_NAME_OLD);
//            dbPath = new File(Environment.getExternalStorageDirectory() + "/emobileposRUM.db");
//            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
//            assignEmployee.setEmpId(1);
//            Realm realm = Realm.getDefaultInstance();
//            realm.beginTransaction();
//            realm.where(AssignEmployee.class).findAll().deleteAllFromRealm();
//            realm.copyToRealm(assignEmployee);
//            realm.commitTransaction();
//            myPref.setDeviceID("21e2243f5be84a18");
//            myPref.setAcctNumber("150872170602");
//            myPref.setAcctPassword("rum123");
//            myPref.setActivKey("31295R1401263065748Y79004A");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        File dbCipherPath = context.getDatabasePath(CIPHER_DB_NAME);
        if (dbPath != null && dbPath.exists() && !dbCipherPath.exists()) {
            try {
                encrypt(context, DB_NAME_OLD, getPassword());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void dbRestore() {
        File dbPath = null;
        try {
            dbPath = context.getDatabasePath(DB_NAME_OLD);
//            dbPath = new File(Environment.getExternalStorageDirectory() + "/" + DB_NAME_OLD);
//            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
//            assignEmployee.setEmpId(1);
//            Realm realm = Realm.getDefaultInstance();
//            realm.beginTransaction();
//            realm.where(AssignEmployee.class).findAll().deleteAllFromRealm();
//            realm.copyToRealm(assignEmployee);
//            realm.commitTransaction();
//            myPref.setDeviceID("21e2243f5be84a18");
//            myPref.setAcctNumber("150872170602");
//            myPref.setAcctPassword("rum123");
//            myPref.setActivKey("31295R1401263065748Y79004A");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        File dbCipherPath = context.getDatabasePath(CIPHER_DB_NAME);
        boolean delete = dbCipherPath.delete();
        if (dbPath != null && dbPath.exists() && !dbCipherPath.exists()) {
            try {
                encrypt(context, DB_NAME_OLD, getPassword());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void exportDBFile() {
        try {
            decrypt(context, CIPHER_DB_NAME, getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        File dbFile = null;
//        try {
//            dbFile = context.getDatabasePath(CIPHER_DB_NAME);
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        File outFile = new File(Environment.getExternalStorageDirectory() + "/emobilepos.sqlite");
//        try {
//            FileUtils.copyFile(dbFile, outFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public boolean isNewDBVersion() {
        try {
            int i = getDatabase().getVersion();
            if (VERSION > i) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public Context getContext() {
        return this.context;
    }

    public void updateDB() {
        this.DBHelper = new DatabaseHelper(this.context);
        this.DBHelper.getWritableDatabase(getPassword());
    }

    public void deleteAllTablesData() {
        try {
            for (String table : TABLE_NAME) {
                getDatabase().execSQL("DELETE FROM " + table);
            }
        } catch (Exception e) {
        }
    }

    public void alterTables() {
        int i = 0;
        for (String table : TABLE_NAME) {
            Cursor cursor = getDatabase().rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'", new String[]{});
            if (cursor.getCount() == 0) {
                getDatabase().execSQL(CREATE_TABLE[i]);
            }
            i++;
        }

        Cursor cursor = getDatabase().rawQuery("select * from  [Orders] limit 1", new String[]{});
        boolean exist = cursor.getColumnIndex("ord_timeStarted") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN [ord_timeStarted] [datetime] NULL");
        }
        exist = cursor.getColumnIndex("prod_prices_group_id") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN [prod_prices_group_id] [varchar](50) NULL");
        }
        exist = cursor.getColumnIndex("bixolonTransactionId") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN [bixolonTransactionId] [varchar](50) NULL");
        }
        exist = cursor.getColumnIndex("assignedTable") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN [assignedTable] [varchar](10) NULL");
        }
        exist = cursor.getColumnIndex("numberOfSeats") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN  [numberOfSeats] [int] NULL");
        }
        exist = cursor.getColumnIndex("associateID") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN  associateID [varchar](10) NULL");
        }
        exist = cursor.getColumnIndex("orderAttributes") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Orders] ADD COLUMN [orderAttributes] [varchar](1000) NULL");
        }
        cursor = getDatabase().rawQuery("select * from  [Products] limit 1", new String[]{});
        exist = cursor.getColumnIndex("isGC") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Products] ADD COLUMN [isGC] [bit] NULL");
        }
        cursor = getDatabase().rawQuery("select * from  [OrderProduct] limit 1", new String[]{});
        exist = cursor.getColumnIndex("prod_sku") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [prod_sku] [varchar](255) NULL");
        }
        exist = cursor.getColumnIndex("product_taxes_json") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [product_taxes_json][varchar]");
        }

        exist = cursor.getColumnIndex("prod_upc") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [prod_upc] [varchar](50) NULL");
        }
        exist = cursor.getColumnIndex("assignedSeat") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [assignedSeat] [varchar](10)");
        }
        exist = cursor.getColumnIndex("seatGroupId") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [seatGroupId][int] NULL");
        }
        exist = cursor.getColumnIndex("addon_ordprod_id") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN  [addon_ordprod_id] [varchar](50)");
        }
        exist = cursor.getColumnIndex("prod_price_points") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [prod_price_points] [int] NULL");
        }
        exist = cursor.getColumnIndex("isGC") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [OrderProduct] ADD COLUMN [isGC] [bit] NULL");
        }


        cursor = getDatabase().rawQuery("select * from  [Payments] limit 1", new String[]{});
        exist = cursor.getColumnIndex("EMV_JSON") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Payments] ADD COLUMN [EMV_JSON] VARCHAR");
        }
        exist = cursor.getColumnIndex("amount_tender") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Payments] ADD COLUMN [amount_tender] money");
        }
        cursor = getDatabase().rawQuery("select * from  [PaymentsDeclined] limit 1", new String[]{});
        exist = cursor.getColumnIndex("EMV_JSON") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [PaymentsDeclined] ADD COLUMN [EMV_JSON] VARCHAR");
        }
        exist = cursor.getColumnIndex("amount_tender") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [PaymentsDeclined] ADD COLUMN [amount_tender] money");
        }
        cursor = getDatabase().rawQuery("select * from  [StoredPayments] limit 1", new String[]{});
        exist = cursor.getColumnIndex("EMV_JSON") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [StoredPayments] ADD COLUMN [EMV_JSON] VARCHAR");
        }
        cursor = getDatabase().rawQuery("select * from  [Payments] limit 1", new String[]{});
        exist = cursor.getColumnIndex("pay_signature_issync") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [Payments] ADD COLUMN [pay_signature_issync] tinyint DEFAULT 0");
        }
        exist = cursor.getColumnIndex("amount_tender") > -1;
        if (!exist) {
            getDatabase().execSQL("ALTER TABLE [StoredPayments] ADD COLUMN [amount_tender] money");
        }

        for (String sql : CREATE_INDEX) {
            getDatabase().execSQL(sql);
        }
    }

    public boolean unsynchItemsLeft() {
        CustomersHandler custHandler = new CustomersHandler(context);
        OrdersHandler ordersHandler = new OrdersHandler(context);
        PaymentsHandler payHandler = new PaymentsHandler(context);
        TemplateHandler templateHandler = new TemplateHandler(context);
        ConsignmentTransactionHandler consHandler = new ConsignmentTransactionHandler(context);
        return custHandler.unsyncCustomersLeft() || ordersHandler.unsyncOrdersLeft() || payHandler.unsyncPaymentsLeft()
                || templateHandler.unsyncTemplatesLeft() || consHandler.unsyncConsignmentsLeft();
    }

    public void forceSend(Activity activity) {
        SynchMethods sm = new SynchMethods(managerInstance);
        sm.synchForceSend(activity);
    }

    public void synchDownloadOnHoldDetails(Intent intent, String ordID, int type, Activity activity) {
        SynchMethods sm = new SynchMethods(managerInstance);
        sm.synchGetOnHoldDetails(type, intent, ordID, activity);
    }

    private class DatabaseHelper extends net.sqlcipher.database.SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, CIPHER_DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String tblName : CREATE_TABLE) {
                db.execSQL(tblName);
            }
            for (String sql : CREATE_INDEX) {
                db.execSQL(sql);
            }
            if (getDatabase() != null && getDatabase().isOpen())
                getDatabase().close();
            myPref.setDBpath(db.getPath());
            setDatabase(db);
//            SynchMethods sm = new SynchMethods(managerInstance);
//            sm.synchReceive(type, context);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //drop all the tables and recreate them
            for (String tblName : TABLE_NAME) {
                db.execSQL("DROP TABLE IF EXISTS " + tblName);
            }
            onCreate(db);
        }
    }

}
