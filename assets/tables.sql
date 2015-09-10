
CREATE TABLE [Product_addons](
	[rest_addons] [int] PRIMARY KEY NOT NULL,
	[prod_id] [varchar](50) NULL,
	[cat_id] [varchar](50) NULL,
	[isactive] [bit] NULL,
	[_update] [datetime] NULL,

 CONSTRAINT [PK_Product_addons] PRIMARY KEY CLUSTERED 
(
	[rest_addons] ASC
)
)




CREATE TABLE [ProdCatXref](
	[idKey] [int] PRIMARY KEY NOT NULL,
	[prod_id] [varchar](50) NOT NULL,
	[cat_id] [varchar](50) NOT NULL,
	[_update] [datetime] NULL,
	[isactive] [bit] NOT NULL,
 CONSTRAINT [PK_ProdCatXref] PRIMARY KEY CLUSTERED 
(
	[idKey] ASC
)
)




CREATE UNIQUE INDEX [IX_ProdCatXref] ON [ProdCatXref] 
(
	[prod_id] ASC,
	[cat_id] ASC
)


CREATE TABLE [Printers_Locations](
	[printerloc_key] [int] PRIMARY KEY NOT NULL,
	[loc_id] [varchar](50) NOT NULL,
	[cat_id] [varchar](50) NOT NULL,
	[printer_id] [int] NOT NULL,

 CONSTRAINT [PK_Printers_Locations] PRIMARY KEY CLUSTERED 
(
	[printerloc_key] ASC
)
)

CREATE UNIQUE INDEX [IX_Printers_Locations] ON [Printers_Locations] 
(
	[printer_id] ASC,
	[loc_id] ASC,
	[cat_id] ASC
)


CREATE TABLE [Printers](
	[printer_id] [int] PRIMARY KEY NOT NULL,
	[printer_name] [varchar](255) NOT NULL,
	[printer_ip] [varchar](255) NOT NULL,
	[printer_port] [varchar](10) NULL,
	[printer_type] [varchar](50) NULL,

 CONSTRAINT [PK_Printers] PRIMARY KEY CLUSTERED 
(
	[printer_id] ASC
)
)


CREATE UNIQUE INDEX [IX_Printers_Name] ON [Printers] 
(
	[printer_name] ASC
)





CREATE TABLE [PriceLevelItems](
	[pricelevel_prod_id] [varchar](50) NOT NULL,
	[pricelevel_id] [varchar](50) NOT NULL,
	[pricelevel] [varchar](50) NULL,
	[pricelevel_price] [money] NOT NULL,
	[pricelevel_update] [datetime] NOT NULL,
	[isactive] [tinyint] NOT NULL,
	PRIMARY KEY (pricelevel_prod_id,pricelevel_id)

 CONSTRAINT [PK_PriceLevelItems] PRIMARY KEY CLUSTERED 
(
	[pricelevel_prod_id] ASC,
	[pricelevel_id] ASC
)
)




CREATE TABLE [PriceLevel](
	[pricelevel_id] [varchar](50) PRIMARY KEY NOT NULL,
	[pricelevel_name] [varchar](50) NULL,
	[pricelevel_type] [varchar](50) NULL,
	[pricelevel_fixedpct] [float] NULL,
	[pricelevel_update] [datetime] NOT NULL,
	[isactive] [tinyint] NOT NULL,
 CONSTRAINT [PK_PriceLevel] PRIMARY KEY CLUSTERED 
(
	[pricelevel_id] ASC
)
)



CREATE TABLE [PayMethods](
	[paymethod_id] [varchar](50) PRIMARY KEY NOT NULL,
	[paymethod_name] [varchar](255) NOT NULL,
	[paymentmethod_type] [varchar](50) NULL,
	[paymethod_update] [datetime] NOT NULL,
	[isactive] [tinyint] NOT NULL,
	[paymethod_showOnline] [tinyint] NULL,
 CONSTRAINT [PK_PayMethods] PRIMARY KEY CLUSTERED 
(
	[paymethod_id] ASC
)
)


CREATE TABLE [Terms](
	[terms_id] [varchar](50) PRIMARY KEY NOT NULL,
	[terms_name] [varchar](255) NOT NULL,
	[terms_stdduedays] [int] NULL,
	[terms_stddiscdays] [int] NULL,
	[terms_discpct] [float] NULL,
	[isactive] [tinyint] NOT NULL,
	[terms_update] [datetime] NOT NULL,
 CONSTRAINT [PK_Terms] PRIMARY KEY CLUSTERED 
(
	[terms_id] ASC
)
)


CREATE TABLE [Taxes_Group](
	[taxGroupKey] [int] PRIMARY KEY NOT NULL,
	[taxGroupId] [varchar](50) NOT NULL,
	[taxId] [varchar](50) NOT NULL,
	[taxcode_id] [varchar](50) NULL,
	[tax_rate] [nchar](10) NULL,
	[taxLowRange] [money] NULL,
	[taxHighRange] [money] NULL,
	[taxgroup_update] [datetime] NULL,
	[isactive] [tinyint] NULL,
 CONSTRAINT [PK_Taxes_Group] PRIMARY KEY CLUSTERED 
(
	[taxGroupKey] ASC
)
)



CREATE TABLE [Taxes](
	[tax_id_key] PRIMARY KEY NOT NULL,
	[tax_id] [varchar](50) NOT NULL,
	[tax_name] [varchar](50) NOT NULL,
	[tax_code_id] [varchar](50) NULL,
	[tax_code_name] [varchar](50) NULL,
	[tax_rate] [float] NOT NULL,
	[tax_type] [char](1) NULL,
	[isactive] [tinyint] NOT NULL,
	[tax_update] [datetime] NOT NULL,
	[prTax] [varchar](4) NULL,
	[tax_default] [tinyint] NULL,
	[tax_account] [varchar](50) NULL,
 CONSTRAINT [PK_Taxes] PRIMARY KEY CLUSTERED 
(
	[tax_id_key] ASC
)
)







CREATE TABLE [ShipMethod](
	[shipmethod_id] [varchar](50) PRIMARY KEY NOT NULL,
	[shipmethod_name] [varchar](255) NOT NULL,
	[isactive] [tinyint] NOT NULL,
	[shipmethod_update] [datetime] NOT NULL,
 CONSTRAINT [PK_ShipMethod] PRIMARY KEY CLUSTERED 
(
	[shipmethod_id] ASC
)
)




CREATE TABLE [SalesTaxCodes](
	[taxcode_id] [varchar](50) PRIMARY KEY NOT NULL,
	[taxcode_name] [varchar](255) NOT NULL,
	[taxcode_desc] [varchar](255) NOT NULL,
	[taxcode_istaxable] [int] NOT NULL,
	[isactive] [tinyint] NOT NULL,
	[taxcode_update] [datetime] NOT NULL,
 CONSTRAINT [PK_SalesTaxCodes] PRIMARY KEY CLUSTERED 
(
	[taxcode_id] ASC
)
)




CREATE TABLE [Reasons](
	[reason_id] [int] PRIMARY KEY NOT NULL,
	[reason_name] [varchar](50) NULL,
	[reason_date] [datetime] NULL,
	[isactive] [tinyint] NULL,
 CONSTRAINT [PK_Reasons] PRIMARY KEY CLUSTERED 
(
	[reason_id] ASC
)
)




CREATE TABLE [PublicVariables](
	[MSEmployeeID] [int] PRIMARY KEY NOT NULL,
	[MSEmployeeName] [nvarchar](50) NULL,
	[MSDeviceID] [nvarchar](255) NULL,
	[MSZoneID] [nvarchar](50) NULL,
	[MSLastSynch] [datetime] NULL,
	[MSOrderLastSynch] [datetime] NULL,
	[MSActivationKey] [nvarchar](27) NULL,
	[MSConnection] [nvarchar](50) NULL,
	[MSTicket] [nvarchar](50) NULL,
	[MSRegID] [nvarchar](50) NULL,
	[MSAccount] [nvarchar](50) NULL,
	[MSUser] [nvarchar](50) NULL,
	[MSPass] [nvarchar](50) NULL,
	[MPUser] [nvarchar](50) NULL,
	[MPPass] [nvarchar](50) NULL,
	[MSQBMS] [int] NULL,
	[MSLastOrderID] [nvarchar](50) NULL,
	[MSOrderEntry] [nvarchar](12) NULL,
	[MSOrderType] [nvarchar](12) NULL,
	[MSCardProcessor] [nvarchar](1) NULL,
	[MSPrinter] [int] NULL,
	[MSLanguage] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[MSEmployeeID] ASC
)
)



CREATE TABLE [Products_Images](
	[img_id] [int] PRIMARY KEY NOT NULL,
	[prod_id] [varchar](50) NOT NULL,
	[prod_img_name] [varchar](255) NOT NULL,
	[prod_default] [tinyint] NOT NULL,
	[type] [nchar](1) NULL,
 CONSTRAINT [PK_Products_Images_1] PRIMARY KEY CLUSTERED 
(
	[img_id] ASC
)
)




CREATE  INDEX [IX_podimages_Select] ON [Products_Images] 
(
	[prod_id] ASC,
	[type] ASC
)




CREATE TABLE [products_attrs](
	[prodAttrKey] [int] PRIMARY KEY NOT NULL,
	[prod_id] [varchar](50) NOT NULL,
	[attr_id] [varchar](50) NOT NULL,
	[attr_name] [varchar](50) NULL,
	[attr_desc] [varchar](50) NULL,
	[attr_group] [varchar](50) NULL,
	[attr_group_id] [varchar](50) NULL,
 CONSTRAINT [PK_product_attrs] PRIMARY KEY CLUSTERED 
(
	[prodAttrKey] ASC
)
)



CREATE UNIQUE INDEX [IX_products_attrs] ON [products_attrs] 
(
	[prod_id] ASC,
	[attr_id] ASC
)


CREATE TABLE [memotext](
	[memo_id] [int] PRIMARY KEY NOT NULL,
	[memo_headerLine1] [varchar](100) NULL,
	[memo_headerLine2] [varchar](100) NULL,
	[memo_headerLine3] [varchar](100) NULL,
	[memo_footerLine1] [varchar](100) NULL,
	[memo_footerLine2] [varchar](100) NULL,
	[memo_footerLine3] [varchar](100) NULL,
	[store_name] [varchar](100) NULL,
	[store_email] [varchar](100) NULL,
	[isactive] [tinyint] NULL,
 CONSTRAINT [PK_memotext] PRIMARY KEY CLUSTERED 
(
	[memo_id] ASC
)
)



CREATE TABLE [InvProducts](
	[ordprod_id] [varchar](255) PRIMARY KEY NOT NULL,
	[prod_id] [varchar](50) NOT NULL,
	[ord_id] [varchar](50) NOT NULL,
	[ordprod_qty] [real] NOT NULL,
	[overwrite_price] [money] NOT NULL,
	[reason_id] [int] NULL,
	[ordprod_desc] [varchar](4095) NULL,
	[pricelevel_id] [varchar](50) NULL,
	[prod_seq] [int] NULL,
	[uom_name] [varchar](50) NULL,
	[uom_conversion] [real] NULL,
 CONSTRAINT [PK_InvProducts] PRIMARY KEY CLUSTERED 
(
	[ordprod_id] ASC
)
)




CREATE TABLE [Invoices](
	[inv_id] [varchar](50) PRIMARY KEY NOT NULL,
	[cust_id] [varchar](50) NULL,
	[emp_id] [int] NULL,
	[inv_timecreated] [datetime] NULL,
	[inv_ispending] [int] NULL,
	[inv_ponumber] [varchar](50) NULL,
	[inv_terms] [varchar](2000) NULL,
	[inv_duedate] [datetime] NULL,
	[inv_shipdate] [datetime] NULL,
	[inv_shipmethod] [varchar](255) NULL,
	[inv_total] [money] NULL,
	[inv_apptotal] [money] NULL,
	[inv_balance] [money] NULL,
	[inv_custmsg] [varchar](2000) NULL,
	[inv_ispaid] [int] NULL,
	[inv_paiddate] [datetime] NULL,
	[mod_date] [datetime] NULL,
	[txnID] [varchar](255) NULL,
	[inv_update] [datetime] NULL,
 CONSTRAINT [PK_Invoices] PRIMARY KEY CLUSTERED 
(
	[inv_id] ASC
)
)



CREATE TABLE [Employees](
	[emp_id] [int] PRIMARY KEY NOT NULL,
	[zone_id] [varchar](50) NULL,
	[emp_name] [varchar](50) NULL,
	[emp_init] [varchar](50) NULL,
	[emp_pcs] [varchar](50) NULL,
	[emp_carrier] [int] NULL,
	[emp_lastlogin] [smalldatetime] NULL,
	[emp_cleanup] [int] NULL,
	[emp_pos] [int] NULL,
	[qb_emp_id] [varchar](50) NULL,
	[qb_salesrep_id] [varchar](50) NULL,
	[quota_month_goal] [money] NULL,
	[quota_month] [money] NULL,
	[quota_year_goal] [money] NULL,
	[quota_year] [money] NULL,
	[emp_pwd] [nvarchar](50) NULL,
	[isactive] [tinyint] NULL,
	[email] [varchar](255) NULL,
	[classid] [varchar](50) NULL,
	[tax_default] [varchar](50) NULL,
	[pricelevel_id] [varchar](50) NULL,
 CONSTRAINT [PK_Employees] PRIMARY KEY CLUSTERED 
(
	[emp_id] ASC
)
)




CREATE TABLE [deviceDefaultValues](
	[df_id] [int] PRIMARY KEY NOT NULL,
	[ord_po] [varchar](50) NULL,
	[posAdminPassword] [varchar](50) NULL,
	[priceLeveldefault] [varchar](50) NULL,
	[defaultPointsPricePercentage] [int] NULL,
	[defaultPointsValuePercentage] [int] NULL,
 CONSTRAINT [PK_deviceDefaultValues] PRIMARY KEY CLUSTERED 
(
	[df_id] ASC
)
)



CREATE TABLE [Customers](
	
	[cust_id] [varchar](50) PRIMARY KEY NOT NULL,
	[cust_id_ref] [varchar](50) NULL,
	[qb_sync] [tinyint] NULL,
	[zone_id] [varchar](50) NULL,
	[CompanyName] [varchar](41) NULL,
	[Salutation] [varchar](15) NULL,
	[cust_name] [varchar](255) NULL,
	[cust_chain] [int] NULL,
	[cust_balance] [money] NULL,
	[cust_limit] [money] NULL,
	[cust_contact] [varchar](255) NULL,
	[cust_firstName] [varchar](25) NULL,
	[cust_middleName] [varchar](5) NULL,
	[cust_lastName] [varchar](25) NULL,
	[cust_phone] [varchar](21) NULL,
	[cust_email] [varchar](1023) NULL,
	[cust_fax] [varchar](21) NULL,
	[cust_update] [datetime] NULL,
	[isactive] [tinyint] NULL,
	[cust_ordertype] [int] NULL,
	[cust_taxable] [varchar](50) NULL,
	[cust_salestaxcode] [varchar](50) NULL,
	[pricelevel_id] [varchar](50) NULL,
	[cust_terms] [varchar](50) NULL,
	[cust_pwd] [varchar](50) NULL,
	[cust_securityquestion] [varchar](150) NULL,
	[cust_securityanswer] [varchar](50) NULL,
	[cust_points] [int] NULL,
 CONSTRAINT [PK_Customers] PRIMARY KEY CLUSTERED 
(
	[custidkey] ASC
)
)



CREATE INDEX [IX_BO_CustomerSearch] ON [Customers] 
(
	[cust_name] ASC,
	[cust_phone] ASC
)


CREATE UNIQUE INDEX [IX_Customers] ON [Customers] 
(
	[cust_id] ASC
)



CREATE TABLE [Categories](
	[cat_id] [varchar](50) PRIMARY KEY NOT NULL,
	[cat_name] [varchar](255) NOT NULL,
	[cat_update] [datetime] NOT NULL,
	[isactive] [tinyint] NOT NULL,
	[parentID] [varchar](50) NULL,
 CONSTRAINT [PK_Categories] PRIMARY KEY CLUSTERED 
(
	[cat_id] ASC
)
)


CREATE TABLE [Address](
	[addr_id] [varchar](50) PRIMARY KEY NOT NULL,
	[cust_id] [varchar](50) NOT NULL,
	[zone_id] [varchar](50) NULL,
	[addr_type] [int] NULL,
	[addr_b_str1] [varchar](41) NULL,
	[addr_b_str2] [varchar](41) NULL,
	[addr_b_str3] [varchar](41) NULL,
	[addr_b_city] [varchar](31) NULL,
	[addr_b_state] [varchar](21) NULL,
	[addr_b_country] [varchar](31) NULL,
	[addr_b_zipcode] [varchar](13) NULL,
	[addr_s_name] [varchar](50) NULL,
	[addr_s_str1] [varchar](41) NULL,
	[addr_s_str2] [varchar](41) NULL,
	[addr_s_str3] [varchar](41) NULL,
	[addr_s_city] [varchar](31) NULL,
	[addr_s_state] [varchar](21) NULL,
	[addr_s_country] [varchar](31) NULL,
	[addr_s_zipcode] [varchar](13) NULL,
	[qb_cust_id] [varchar](50) NULL,
	PRIMARY KEY (addr_id,cust_id))
 CONSTRAINT [PK_Address] PRIMARY KEY CLUSTERED 
(
	[addr_id] ASC,
	[cust_id] ASC
)
)


CREATE INDEX [IX_Address_custid] ON [dbo].[Address] 
(
	[cust_id] ASC
)




CREATE TABLE [Products](
	[prod_id] [varchar](50) PRIMARY KEY NOT NULL,
	[prod_type] [varchar](50) NULL,
	[prod_disc_type] [varchar](50) NULL,
	[cat_id] [varchar](50) NULL,
	[prod_sku] [varchar](255) NULL,
	[prod_upc] [varchar](50) NULL,
	[prod_name] [varchar](255) NULL,
	[prod_desc] [varchar](4000) NULL,
	[prod_extradesc] [varchar](255) NULL,
	[prod_onhand] [real] NULL,
	[prod_onorder] [real] NULL,
	[prod_uom] [varchar](50) NULL,
	[prod_price] [money] NULL,
	[prod_cost] [money] NULL,
	[prod_taxcode] [varchar](50) NULL,
	[prod_taxtype] [varchar](50) NULL,
	[prod_glaccount] [varchar](50) NULL,
	[prod_mininv] [int] NULL,
	[prod_update] [datetime] NULL,
	[isactive] [int] NULL,
	[prod_showOnline] [tinyint] NULL,
	[prod_ispromo] [tinyint] NULL,
	[prod_shipping] [tinyint] NULL,
	[prod_weight] [real] NULL,
	[prod_expense] [bit] NULL,
	[prod_disc_type_points] [varchar](255) NULL,
	[prod_price_points] [int] NULL,
	[prod_value_points] [int] NULL,
 CONSTRAINT [PK_Products] PRIMARY KEY CLUSTERED 
(
	[prod_id] ASC
)
)


CREATE INDEX [IX_products_catid] ON [Products] 
(
	[cat_id] ASC
)



CREATE INDEX [IX_Products_isactive] ON [Products] 
(
	[isactive] ASC
)


CREATE INDEX [IX_Products_prodname] ON [Products] 
(
	[prod_name] ASC
)


CREATE INDEX [IX_Products_prodtype] ON [Products] 
(
	[prod_type] ASC
)





CREATE TABLE [Orders](
	[ord_id] [varchar](50) PRIMARY KEY NOT NULL,
	[qbord_id] [varchar](50) NULL,
	[qbtxid] [varchar](255) NULL,
	[emp_id] [int] NULL,
	[cust_id] [varchar](50) NULL,
	[ord_po] [varchar](50) NULL,
	[total_lines] [int] NULL,
	[total_lines_pay] [int] NULL,
	[ord_total] [money] NULL,
	[ord_signature] [image] NULL,
	[ord_comment] [varchar](255) NULL,
	[ord_delivery] [datetime] NULL,
	[ord_timecreated] [datetime] NULL,
	[ord_timesync] [datetime] NULL,
	[qb_synctime] [datetime] NULL,
	[emailed] [int] NULL,
	[processed] [int] NULL,
	[ord_type] [int] NULL,
	[ord_claimnumber] [varchar](50) NULL,
	[ord_rganumber] [varchar](50) NULL,
	[ord_returns_pu] [tinyint] NULL,
	[ord_inventory] [tinyint] NULL,
	[ord_issync] [tinyint] NULL,
	[tax_id] [varchar](50) NULL,
	[ord_shipvia] [varchar](50) NULL,
	[ord_shipto] [varchar](50) NULL,
	[ord_terms] [varchar](50) NULL,
	[ord_custmsg] [varchar](50) NULL,
	[ord_class] [varchar](50) NULL,
	[ord_subtotal] [money] NULL,
	[ord_taxamount] [money] NULL,
	[ord_discount] [money] NULL,
	[user_ID] [int] NULL,
	[addr_b_str1] [varchar](41) NULL,
	[addr_b_str2] [varchar](41) NULL,
	[addr_b_str3] [varchar](41) NULL,
	[addr_b_city] [varchar](31) NULL,
	[addr_b_state] [varchar](21) NULL,
	[addr_b_country] [varchar](31) NULL,
	[addr_b_zipcode] [varchar](13) NULL,
	[addr_s_str1] [varchar](41) NULL,
	[addr_s_str2] [varchar](41) NULL,
	[addr_s_str3] [varchar](41) NULL,
	[addr_s_city] [varchar](31) NULL,
	[addr_s_state] [varchar](21) NULL,
	[addr_s_country] [varchar](31) NULL,
	[addr_s_zipcode] [varchar](13) NULL,
	[c_email] [varchar](100) NULL,
	[loc_id] [varchar](50) NULL,
	[ord_HoldName] [varchar](50) NULL,
 CONSTRAINT [PK_Orders] PRIMARY KEY CLUSTERED 
(
	[ord_id] ASC
)
)



CREATE INDEX [IX_Orders_empid_Custid] ON [Orders] 
(
	[emp_id] ASC,
	[cust_id] ASC
)


CREATE INDEX [IX_Orders_ordtimeCreated] ON [Orders] 
(
	[ord_timecreated] ASC
)


CREATE INDEX [IX_Orders_ordType] ON [Orders] 
(
	[ord_type] ASC
)


CREATE INDEX [IX_Orders_processed] ON [Orders] 
(
	[processed] ASC
)



CREATE TABLE [OrderProducts](
	[ordprod_id] [uniqueidentifier] PRIMARY KEY NOT NULL,
	[prod_id] [varchar](50) NOT NULL,
	[ord_id] [varchar](50) NOT NULL,
	[ordprod_qty] [real] NOT NULL,
	[overwrite_price] [money] NOT NULL,
	[reason_id] [int] NULL,
	[ordprod_desc] [varchar](4095) NULL,
	[pricelevel_id] [varchar](50) NULL,
	[prod_seq] [int] NULL,
	[uom_name] [varchar](50) NULL,
	[uom_conversion] [real] NULL,
	[discount_id] [varchar](50) NULL,
	[discount_value] [money] NULL,
	[item_void] [tinyint] NULL,
	[isPrinted] [bit] NULL,
	[cat_id] [varchar](50) NULL,
	[cat_name] [varchar](50) NULL,
	[addon] [bit] NULL,
	[isAdded] [bit] NULL,
 CONSTRAINT [PK_OrderProducts1] PRIMARY KEY CLUSTERED 
(
	[ordprod_id] ASC
)
)


CREATE INDEX [IX_BO_TransactionOpenDetail_Select] ON [OrderProducts] 
(
	[prod_id] ASC,
	[ord_id] ASC,
	[prod_seq] ASC
)


CREATE INDEX [IX_orderproducts_prodid] ON [OrderProducts] 
(
	[prod_id] ASC
)


CREATE TABLE [ProductChainXRef](
	[chainKey] [uniqueidentifier] PRIMARY KEY NOT NULL,
	[cust_chain] [varchar](50) NOT NULL,
	[prod_id] [varchar](50) NOT NULL,
	[over_price_gross] [money] NULL,
	[over_price_net] [money] NOT NULL,
	[isactive] [tinyint] NOT NULL,
	[productchain_update] [datetime] NULL,
	[customer_item] [varchar](20) NULL,
 CONSTRAINT [PK_ProductChainXRef] PRIMARY KEY CLUSTERED 
(
	[chainKey] ASC
)
)

CREATE UNIQUE INDEX [IX_ProductChainXRef] ON [ProductChainXRef] 
(
	[cust_chain] ASC,
	[prod_id] ASC
)


CREATE TABLE [Payments](
	[pay_id] [varchar](50) PRIMARY KEY NOT NULL,
	[group_pay_id] [varchar](50) NULL,
	[cust_id] [varchar](50) NULL,
	[emp_id] [int] NULL,
	[inv_id] [varchar](50) NULL,
	[paymethod_id] [varchar](50) NULL,
	[pay_check] [varchar](50) NULL,
	[pay_receipt] [varchar](50) NULL,
	[pay_amount] [money] NULL,
	[pay_comment] [varchar](2000) NULL,
	[pay_timecreated] [datetime] NULL,
	[pay_timesync] [datetime] NULL,
	[account_id] [varchar](50) NULL,
	[processed] [int] NULL,
	[pay_issync] [int] NULL,
	[pay_transid] [varchar](50) NULL,
	[pay_refnum] [varchar](50) NULL,
	[pay_name] [varchar](50) NULL,
	[pay_addr] [varchar](50) NULL,
	[pay_poscode] [varchar](50) NULL,
	[pay_seccode] [varchar](50) NULL,
	[pay_maccount] [varchar](50) NULL,
	[pay_groupcode] [varchar](50) NULL,
	[pay_stamp] [varchar](50) NULL,
	[pay_resultcode] [varchar](50) NULL,
	[pay_resultmessage] [varchar](500) NULL,
	[pay_ccnum] [varchar](255) NULL,
	[pay_expmonth] [varchar](50) NULL,
	[pay_expyear] [varchar](50) NULL,
	[pay_expdate] [varchar](50) NULL,
	[pay_result] [varchar](255) NULL,
	[pay_date] [datetime] NULL,
	[recordnumber] [varchar](50) NULL,
	[pay_signature] [varchar](50) NULL,
	[authcode] [varchar](50) NULL,
	[status] [int] NULL,
	[job_id] [varchar](50) NULL,
	[user_ID] [int] NULL,
	[pay_type] [int] NULL,
	[pay_tip] [money] NULL,
	[ccnum_last4] [varchar](4) NULL,
	[pay_phone] [varchar](50) NULL,
	[pay_email] [varchar](100) NULL,
 CONSTRAINT [PK_Payments] PRIMARY KEY CLUSTERED 
(
	[pay_id] ASC
)
)

CREATE INDEX [IX_BO_Payments_Select] ON [Payments] 
(
	[cust_id] ASC,
	[emp_id] ASC,
	[paymethod_id] ASC
)


CREATE INDEX [IX_Payments_invid] ON [Payments] 
(
	[inv_id] ASC
)


CREATE INDEX [IX_payments_jobid] ON [Payments] 
(
	[job_id] ASC
)

CREATE INDEX [IX_payments_pay_date] ON [Payments] 
(
	[pay_date] ASC
)


CREATE INDEX [IX_Payments_paymehtodID] ON [Payments] 
(
	[paymethod_id] ASC
)


CREATE INDEX [IX_Payments_Processed] ON [Payments] 
(
	[processed] ASC
)


CREATE INDEX [IX_Payments_transID] ON [Payments] 
(
	[pay_transid] ASC
)



CREATE TABLE [EmpInv](
	[emp_inv_id] [int] PRIMARY KEY NOT NULL,
	[emp_id] [int] NULL,
	[prod_id] [varchar](50) NULL,
	[prod_onhand] [int] NULL,
	[emp_update] [datetime] NULL,
	[issync] [int] NULL,
 CONSTRAINT [PK_EmpInv] PRIMARY KEY CLUSTERED 
(
	[emp_inv_id] ASC
)
)


CREATE UNIQUE INDEX [IX_empinv] ON [EmpInv] 
(
	[emp_id] ASC,
	[prod_id] ASC
)

GO
/****** Object:  Default [DF_Categories_cat_update]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Categories] ADD  CONSTRAINT [DF_Categories_cat_update]  DEFAULT (getutcdate()) FOR [cat_update]
GO
/****** Object:  Default [DF_Categories_isactive]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Categories] ADD  CONSTRAINT [DF_Categories_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_Customers_custidkey]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Customers] ADD  CONSTRAINT [DF_Customers_custidkey]  DEFAULT (newid()) FOR [custidkey]
GO
/****** Object:  Default [DF_Customers_qb_sync]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Customers] ADD  CONSTRAINT [DF_Customers_qb_sync]  DEFAULT ((2)) FOR [qb_sync]
GO
/****** Object:  Default [DF_Customers_cust_update]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Customers] ADD  CONSTRAINT [DF_Customers_cust_update]  DEFAULT (getutcdate()) FOR [cust_update]
GO
/****** Object:  Default [DF_Customers_isactive]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Customers] ADD  CONSTRAINT [DF_Customers_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_OrderProducts1_ordprod_id]    Script Date: 08/14/2012 09:32:09 ******/
ALTER TABLE [dbo].[OrderProducts] ADD  CONSTRAINT [DF_OrderProducts1_ordprod_id]  DEFAULT (newid()) FOR [ordprod_id]
GO
/****** Object:  Default [DF_OrderProducts_ordprod_desc]    Script Date: 08/14/2012 09:32:10 ******/
ALTER TABLE [dbo].[OrderProducts] ADD  CONSTRAINT [DF_OrderProducts_ordprod_desc]  DEFAULT ('No Description') FOR [ordprod_desc]
GO
/****** Object:  Default [DF_PayMethods_paymethod_update]    Script Date: 08/14/2012 09:33:57 ******/
ALTER TABLE [dbo].[PayMethods] ADD  CONSTRAINT [DF_PayMethods_paymethod_update]  DEFAULT (getutcdate()) FOR [paymethod_update]
GO
/****** Object:  Default [DF_PayMethods_isactive]    Script Date: 08/14/2012 09:33:58 ******/
ALTER TABLE [dbo].[PayMethods] ADD  CONSTRAINT [DF_PayMethods_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_PayMethods_paymethod_showOnline]    Script Date: 08/14/2012 09:33:58 ******/
ALTER TABLE [dbo].[PayMethods] ADD  CONSTRAINT [DF_PayMethods_paymethod_showOnline]  DEFAULT ((0)) FOR [paymethod_showOnline]
GO
/****** Object:  Default [DF_PriceLevel_pricelevel_update]    Script Date: 08/14/2012 09:34:05 ******/
ALTER TABLE [dbo].[PriceLevel] ADD  CONSTRAINT [DF_PriceLevel_pricelevel_update]  DEFAULT (getutcdate()) FOR [pricelevel_update]
GO
/****** Object:  Default [DF_PriceLevel_isactive]    Script Date: 08/14/2012 09:34:06 ******/
ALTER TABLE [dbo].[PriceLevel] ADD  CONSTRAINT [DF_PriceLevel_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_PriceLevelItems_pricelevel_update]    Script Date: 08/14/2012 09:34:16 ******/
ALTER TABLE [dbo].[PriceLevelItems] ADD  CONSTRAINT [DF_PriceLevelItems_pricelevel_update]  DEFAULT (getutcdate()) FOR [pricelevel_update]
GO
/****** Object:  Default [DF_PriceLevelItems_isactive]    Script Date: 08/14/2012 09:34:16 ******/
ALTER TABLE [dbo].[PriceLevelItems] ADD  CONSTRAINT [DF_PriceLevelItems_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_ProductChainXRef_chainKey]    Script Date: 08/14/2012 09:35:02 ******/
ALTER TABLE [dbo].[ProductChainXRef] ADD  CONSTRAINT [DF_ProductChainXRef_chainKey]  DEFAULT (newid()) FOR [chainKey]
GO
/****** Object:  Default [DF_Products_prod_type]    Script Date: 08/14/2012 09:35:33 ******/
ALTER TABLE [dbo].[Products] ADD  CONSTRAINT [DF_Products_prod_type]  DEFAULT ('Inventory') FOR [prod_type]
GO
/****** Object:  Default [DF_Products_prod_update]    Script Date: 08/14/2012 09:35:33 ******/
ALTER TABLE [dbo].[Products] ADD  CONSTRAINT [DF_Products_prod_update]  DEFAULT (getutcdate()) FOR [prod_update]
GO
/****** Object:  Default [DF_Products_prod_isactive]    Script Date: 08/14/2012 09:35:34 ******/
ALTER TABLE [dbo].[Products] ADD  CONSTRAINT [DF_Products_prod_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_Products_prod_showOnline]    Script Date: 08/14/2012 09:35:34 ******/
ALTER TABLE [dbo].[Products] ADD  CONSTRAINT [DF_Products_prod_showOnline]  DEFAULT ((0)) FOR [prod_showOnline]
GO
/****** Object:  Default [DF_Products_prod_ispromo]    Script Date: 08/14/2012 09:35:35 ******/
ALTER TABLE [dbo].[Products] ADD  CONSTRAINT [DF_Products_prod_ispromo]  DEFAULT ((0)) FOR [prod_ispromo]
GO
/****** Object:  Default [DF_SalesTaxCodes_isactive]    Script Date: 08/14/2012 09:36:17 ******/
ALTER TABLE [dbo].[SalesTaxCodes] ADD  CONSTRAINT [DF_SalesTaxCodes_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_SalesTaxCodes_taxcode_update]    Script Date: 08/14/2012 09:36:17 ******/
ALTER TABLE [dbo].[SalesTaxCodes] ADD  CONSTRAINT [DF_SalesTaxCodes_taxcode_update]  DEFAULT (getutcdate()) FOR [taxcode_update]
GO
/****** Object:  Default [DF_Taxes_tax_id_key]    Script Date: 08/14/2012 09:36:36 ******/
ALTER TABLE [dbo].[Taxes] ADD  CONSTRAINT [DF_Taxes_tax_id_key]  DEFAULT (newid()) FOR [tax_id_key]
GO
/****** Object:  Default [DF_Taxes_isactive]    Script Date: 08/14/2012 09:36:37 ******/
ALTER TABLE [dbo].[Taxes] ADD  CONSTRAINT [DF_Taxes_isactive]  DEFAULT ((1)) FOR [isactive]
GO
/****** Object:  Default [DF_Taxes_tax_update]    Script Date: 08/14/2012 09:36:37 ******/
ALTER TABLE [dbo].[Taxes] ADD  CONSTRAINT [DF_Taxes_tax_update]  DEFAULT (getutcdate()) FOR [tax_update]
GO
/****** Object:  ForeignKey [FK_Address_Customers]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[Address]  WITH NOCHECK ADD  CONSTRAINT [FK_Address_Customers] FOREIGN KEY([cust_id])
REFERENCES [dbo].[Customers] ([cust_id])
GO
ALTER TABLE [dbo].[Address] NOCHECK CONSTRAINT [FK_Address_Customers]
GO
/****** Object:  ForeignKey [FK_EmpInv_Employees]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[EmpInv]  WITH NOCHECK ADD  CONSTRAINT [FK_EmpInv_Employees] FOREIGN KEY([emp_id])
REFERENCES [dbo].[Employees] ([emp_id])
GO
ALTER TABLE [dbo].[EmpInv] NOCHECK CONSTRAINT [FK_EmpInv_Employees]
GO
/****** Object:  ForeignKey [FK_EmpInv_Products]    Script Date: 08/14/2012 09:31:37 ******/
ALTER TABLE [dbo].[EmpInv]  WITH NOCHECK ADD  CONSTRAINT [FK_EmpInv_Products] FOREIGN KEY([prod_id])
REFERENCES [dbo].[Products] ([prod_id])
GO
ALTER TABLE [dbo].[EmpInv] NOCHECK CONSTRAINT [FK_EmpInv_Products]
GO
/****** Object:  ForeignKey [FK_OrderProducts_Orders]    Script Date: 08/14/2012 09:32:08 ******/
ALTER TABLE [dbo].[OrderProducts]  WITH NOCHECK ADD  CONSTRAINT [FK_OrderProducts_Orders] FOREIGN KEY([ord_id])
REFERENCES [dbo].[Orders] ([ord_id])
GO
ALTER TABLE [dbo].[OrderProducts] NOCHECK CONSTRAINT [FK_OrderProducts_Orders]
GO
/****** Object:  ForeignKey [FK_OrderProducts_Products]    Script Date: 08/14/2012 09:32:09 ******/
ALTER TABLE [dbo].[OrderProducts]  WITH NOCHECK ADD  CONSTRAINT [FK_OrderProducts_Products] FOREIGN KEY([prod_id])
REFERENCES [dbo].[Products] ([prod_id])
GO
ALTER TABLE [dbo].[OrderProducts] NOCHECK CONSTRAINT [FK_OrderProducts_Products]
GO
/****** Object:  ForeignKey [FK_Orders_Customers]    Script Date: 08/14/2012 09:32:56 ******/
ALTER TABLE [dbo].[Orders]  WITH NOCHECK ADD  CONSTRAINT [FK_Orders_Customers] FOREIGN KEY([cust_id])
REFERENCES [dbo].[Customers] ([cust_id])
GO
ALTER TABLE [dbo].[Orders] NOCHECK CONSTRAINT [FK_Orders_Customers]
GO
/****** Object:  ForeignKey [FK_Orders_Employees]    Script Date: 08/14/2012 09:32:57 ******/
ALTER TABLE [dbo].[Orders]  WITH NOCHECK ADD  CONSTRAINT [FK_Orders_Employees] FOREIGN KEY([emp_id])
REFERENCES [dbo].[Employees] ([emp_id])
GO
ALTER TABLE [dbo].[Orders] NOCHECK CONSTRAINT [FK_Orders_Employees]
GO
/****** Object:  ForeignKey [FK_Payments_Customers]    Script Date: 08/14/2012 09:33:44 ******/
ALTER TABLE [dbo].[Payments]  WITH NOCHECK ADD  CONSTRAINT [FK_Payments_Customers] FOREIGN KEY([cust_id])
REFERENCES [dbo].[Customers] ([cust_id])
GO
ALTER TABLE [dbo].[Payments] NOCHECK CONSTRAINT [FK_Payments_Customers]
GO
/****** Object:  ForeignKey [FK_Payments_Employees]    Script Date: 08/14/2012 09:33:44 ******/
ALTER TABLE [dbo].[Payments]  WITH NOCHECK ADD  CONSTRAINT [FK_Payments_Employees] FOREIGN KEY([emp_id])
REFERENCES [dbo].[Employees] ([emp_id])
GO
ALTER TABLE [dbo].[Payments] NOCHECK CONSTRAINT [FK_Payments_Employees]
GO
/****** Object:  ForeignKey [FK_Payments_Invoices]    Script Date: 08/14/2012 09:33:45 ******/
ALTER TABLE [dbo].[Payments]  WITH NOCHECK ADD  CONSTRAINT [FK_Payments_Invoices] FOREIGN KEY([inv_id])
REFERENCES [dbo].[Invoices] ([inv_id])
GO
ALTER TABLE [dbo].[Payments] NOCHECK CONSTRAINT [FK_Payments_Invoices]
GO
/****** Object:  ForeignKey [FK_Payments_Orders]    Script Date: 08/14/2012 09:33:46 ******/
ALTER TABLE [dbo].[Payments]  WITH NOCHECK ADD  CONSTRAINT [FK_Payments_Orders] FOREIGN KEY([job_id])
REFERENCES [dbo].[Orders] ([ord_id])
GO
ALTER TABLE [dbo].[Payments] NOCHECK CONSTRAINT [FK_Payments_Orders]
GO
/****** Object:  ForeignKey [FK_Payments_PayMethods]    Script Date: 08/14/2012 09:33:47 ******/
ALTER TABLE [dbo].[Payments]  WITH NOCHECK ADD  CONSTRAINT [FK_Payments_PayMethods] FOREIGN KEY([paymethod_id])
REFERENCES [dbo].[PayMethods] ([paymethod_id])
GO
ALTER TABLE [dbo].[Payments] NOCHECK CONSTRAINT [FK_Payments_PayMethods]
GO
/****** Object:  ForeignKey [FK_Payments_signatures]    Script Date: 08/14/2012 09:33:48 ******/
ALTER TABLE [dbo].[Payments]  WITH NOCHECK ADD  CONSTRAINT [FK_Payments_signatures] FOREIGN KEY([pay_transid])
REFERENCES [dbo].[signatures] ([pay_transid])
GO
ALTER TABLE [dbo].[Payments] NOCHECK CONSTRAINT [FK_Payments_signatures]
GO
/****** Object:  ForeignKey [FK_ProductChainXRef_Customers]    Script Date: 08/14/2012 09:35:00 ******/
ALTER TABLE [dbo].[ProductChainXRef]  WITH NOCHECK ADD  CONSTRAINT [FK_ProductChainXRef_Customers] FOREIGN KEY([cust_chain])
REFERENCES [dbo].[Customers] ([cust_id])
GO
ALTER TABLE [dbo].[ProductChainXRef] NOCHECK CONSTRAINT [FK_ProductChainXRef_Customers]
GO
/****** Object:  ForeignKey [FK_ProductChainXRef_Products]    Script Date: 08/14/2012 09:35:01 ******/
ALTER TABLE [dbo].[ProductChainXRef]  WITH NOCHECK ADD  CONSTRAINT [FK_ProductChainXRef_Products] FOREIGN KEY([prod_id])
REFERENCES [dbo].[Products] ([prod_id])
GO
ALTER TABLE [dbo].[ProductChainXRef] NOCHECK CONSTRAINT [FK_ProductChainXRef_Products]
GO
/****** Object:  ForeignKey [FK_Products_Categories]    Script Date: 08/14/2012 09:35:32 ******/
ALTER TABLE [dbo].[Products]  WITH NOCHECK ADD  CONSTRAINT [FK_Products_Categories] FOREIGN KEY([cat_id])
REFERENCES [dbo].[Categories] ([cat_id])
GO
ALTER TABLE [dbo].[Products] NOCHECK CONSTRAINT [FK_Products_Categories]
GO
