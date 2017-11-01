-- ------------------------------------------------------------ MODULES ------------------------------------------------------
INSERT INTO MODULES (module_name,module_tz,module_perm) VALUES('telco-network-planning','Asia/Jakarta','PERM_TELCO_NETWORK_PLANNING');

-- ------------------------------------------------------------ FILTER GROUPS ------------------------------------------------
INSERT INTO FILTER_GROUPS(filter_group_key, filter_group_label, filter_group_parent_id) VALUES ('filterUser', 'User', null);
INSERT INTO FILTER_GROUPS(filter_group_key, filter_group_label, filter_group_parent_id) VALUES ('threshold', 'Threshold', (select filter_group_id from filter_groups where filter_group_key='filterUser'));
INSERT INTO FILTER_GROUPS(filter_group_key, filter_group_label, filter_group_parent_id) VALUES ('filterCell', 'Cell', null);

-- ------------------------------------------------------------ INTERVALS ------------------------------------------------------
INSERT INTO INTERVALS (interval_key,interval_label,interval_perm) VALUES('week','Weekly','PERM_TNP_INTERVAL_WEEKLY');
INSERT INTO INTERVALS (interval_key,interval_label,interval_perm) VALUES('month','Monthly','PERM_TNP_INTERVAL_MONTHLY');

-- ------------------------------------------------------------ MODULES_INTERVALS ------------------------------------------------------
INSERT INTO MODULES_INTERVALS(module_id,interval_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select interval_id from intervals where interval_perm='PERM_TNP_INTERVAL_WEEKLY'));
INSERT INTO MODULES_INTERVALS(module_id,interval_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select interval_id from intervals where interval_perm='PERM_TNP_INTERVAL_MONTHLY'));

-- ------------------------------------------------------------ FILTERS ------------------------------------------------------
INSERT INTO FILTERS (filter_key,filter_label,filter_perm,filter_ui_type,filter_group_id) VALUES('threshold_data_arpu','Data ARPU (IDR)','PERM_TNP_FILTER_THRESHOLD_ARPU','range',(select filter_group_id from filter_groups where filter_group_key='threshold'));
INSERT INTO FILTERS (filter_key,filter_label,filter_perm,filter_ui_type,filter_group_id) VALUES('cell_network','Cell Network','PERM_TNP_FILTER_CELL_NETWORK','tag-cloud',(select filter_group_id from filter_groups where filter_group_key='filterCell'));
INSERT INTO FILTERS (filter_key,filter_label,filter_perm,filter_ui_type,filter_group_id) VALUES('home_location','Home Location','PERM_TNP_FILTER_HOME_LOCATION','multi-select',(select filter_group_id from filter_groups where filter_group_key='filterUser'));
INSERT INTO FILTERS (filter_key,filter_label,filter_perm,filter_ui_type,filter_group_id) VALUES('work_location','Work Location','PERM_TNP_FILTER_WORK_LOCATION','multi-select',(select filter_group_id from filter_groups where filter_group_key='filterUser'));

-- ------------------------------------------------------------ FILTER VALUES ------------------------------------------------------
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='threshold_data_arpu'),'filter.threshold.arpu','[0,2500000]',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='cell_network'),'filter.cellNetwork.2g','2G',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='cell_network'),'filter.cellNetwork.3g','3G',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='cell_network'),'filter.cellNetwork.4g','LTE',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='work_location'),'roiL3:98','RAFFLES PLACE',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='work_location'),'roiL3:249','TAMPINES EAST',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='home_location'),'roiL2:38','HOUGANG',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='home_location'),'roiL2:35','WOODLANDS',null);
INSERT INTO FILTER_VALUES (filter_id,filter_value_key,filter_value_label,filter_value_info) VALUES ((select filter_id from filters where filter_key='home_location'),'roiL2:39','JURONG EAST',null);

-- ------------------------------------------------------------ INDICATORS ------------------------------------------------------
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('calls','Call','PERM_TNP_INDICATOR_CALL');
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('sms','SMS','PERM_TNP_INDICATOR_SMS');
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('home','Home','PERM_TNP_INDICATOR_HOME');
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('work','Work','PERM_TNP_INDICATOR_WORK');
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('unique_people','Unique People','PERM_TNP_INDICATOR_UNIQUE_PPL');
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('total_stay_duration','Total Stay Duration','PERM_TNP_STAY_DURATION');
INSERT INTO INDICATORS (indicator_key,indicator_label,indicator_perm) VALUES('avg_stay_duration','Average Stay Duration','PERM_TNP_AVG_STAY_DUARTION');

-- ------------------------------------------------------------ ROI LEVELS ------------------------------------------------------
INSERT INTO ROI_LEVELS (roi_level_key,roi_level_label,roi_level_perm,roi_level_order) VALUES ('roiL1','Planning Region','PERM_TNP_ROI_PLANNING_REGION',1);
INSERT INTO ROI_LEVELS (roi_level_key,roi_level_label,roi_level_perm,roi_level_order) VALUES ('roiL2','Planning Area','PERM_TNP_ROI_PLANNING_AREA',2);
INSERT INTO ROI_LEVELS (roi_level_key,roi_level_label,roi_level_perm,roi_level_order) VALUES ('roiL3','Subzone','PERM_TNP_ROI_SUBZONE',3);
INSERT INTO ROI_LEVELS (roi_level_key,roi_level_label,roi_level_perm,roi_level_order) VALUES ('roiL4','Submtz','PERM_TNP_ROI_SUBMTZ',4);
INSERT INTO ROI_LEVELS (roi_level_key,roi_level_label,roi_level_perm,roi_level_order) VALUES ('roiL5','All Sites','PERM_TNP_ROI_ALL_SITES',5);

-- ------------------------------------------------------------ MODULES_FILTER_GROUPS ------------------------------------------------------
INSERT INTO MODULES_FILTER_GROUPS(module_id,filter_group_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select filter_group_id from filter_groups where filter_group_key='filterUser'));
INSERT INTO MODULES_FILTER_GROUPS(module_id,filter_group_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select filter_group_id from filter_groups where filter_group_key='filterCell'));

-- ------------------------------------------------------------ MODULES_INDICATORS ------------------------------------------------------
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='calls'));
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='sms'));
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='home'));
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='work'));
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='unique_people'));
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='total_stay_duration'));
INSERT INTO MODULES_INDICATORS(module_id,indicator_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select indicator_id from indicators where indicator_key='avg_stay_duration'));

-- ------------------------------------------------------------ MODULES_ROI_LEVELS ------------------------------------------------------
INSERT INTO MODULES_ROI_LEVELS(module_id,roi_level_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select roi_level_id from roi_levels where roi_level_key='roiL1'));
INSERT INTO MODULES_ROI_LEVELS(module_id,roi_level_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select roi_level_id from roi_levels where roi_level_key='roiL2'));
INSERT INTO MODULES_ROI_LEVELS(module_id,roi_level_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select roi_level_id from roi_levels where roi_level_key='roiL3'));
INSERT INTO MODULES_ROI_LEVELS(module_id,roi_level_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select roi_level_id from roi_levels where roi_level_key='roiL4'));
INSERT INTO MODULES_ROI_LEVELS(module_id,roi_level_id) VALUES((select module_id from modules where module_name='telco-network-planning'),(select roi_level_id from roi_levels where roi_level_key='roiL5'));

-- ------------------------------------------------------------ ROI VALUES ------------------------------------------------------
INSERT INTO ROI_VALUES (roi_level_id,roi_value_key,roi_value_label,roi_value_perm) VALUES ((select roi_level_id from roi_levels where roi_level_perm='PERM_TNP_ROI_PLANNING_REGION'),'1', 'CENTRAL REGION','PERM_TNP_ROI_PLANNING_REGION_1');
INSERT INTO ROI_VALUES (roi_level_id,roi_value_key,roi_value_label,roi_value_perm) VALUES ((select roi_level_id from roi_levels where roi_level_perm='PERM_TNP_ROI_PLANNING_REGION'),'2', 'EAST REGION','PERM_TNP_ROI_PLANNING_REGION_2');
INSERT INTO ROI_VALUES (roi_level_id,roi_value_key,roi_value_label,roi_value_perm) VALUES ((select roi_level_id from roi_levels where roi_level_perm='PERM_TNP_ROI_PLANNING_REGION'),'3', 'NORTH REGION','PERM_TNP_ROI_PLANNING_REGION_3');
INSERT INTO ROI_VALUES (roi_level_id,roi_value_key,roi_value_label,roi_value_perm) VALUES ((select roi_level_id from roi_levels where roi_level_perm='PERM_TNP_ROI_PLANNING_REGION'),'4', 'NORTH-EAST REGION','PERM_TNP_ROI_PLANNING_REGION_4');
INSERT INTO ROI_VALUES (roi_level_id,roi_value_key,roi_value_label,roi_value_perm) VALUES ((select roi_level_id from roi_levels where roi_level_perm='PERM_TNP_ROI_PLANNING_REGION'),'5', 'WEST REGION','PERM_TNP_ROI_PLANNING_REGION_5');