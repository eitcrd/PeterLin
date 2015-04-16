package org.apache.nutch.parse.s2jh;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.google.common.collect.Lists;

public class PayeasyHtmlParseFilter extends AbstractHtmlParseFilter {

	public static final Logger LOG = LoggerFactory
			.getLogger(PayeasyHtmlParseFilter.class);

	@Override
	public Parse filterInternal(String url, WebPage page, Parse parse,
			HTMLMetaTags metaTags, DocumentFragment doc) {

		System.out.println("####PayeasyHtmlParseFilter");

		try {

			if (StringUtils.isNotBlank(getXPathValue(doc,
					"//DIV[@class='product_info_area']/H1"))) {
				// 開始擷取所需資料
				 
				CrawlData crawlDatas = new CrawlData();

				String title = getXPathValue(doc,
						"//DIV[@class='product_info_area']/H1");
				crawlDatas.setTitle(title);

				// 建議市價
				String marketPrice = getXPathValue(doc,
						"//SPAN[@class='price_002a']");
				if (StringUtils.isBlank(marketPrice)) {
					marketPrice = getXPathValue(doc,
							"//DIV[@class='pro_pg_cp']");
				}
				LOG.info("marketPrice:" + marketPrice);
		 
				crawlDatas.setPrice(new BigDecimal(StringUtils.replace(marketPrice, ",", "")));
				// 銷售價
				String promotionsPrice = getXPathValue(doc,
						"//DIV[@class='promotionsPrice']");
				if (StringUtils.isBlank(promotionsPrice)) {
					promotionsPrice = getXPathValue(doc,
							"//DIV[@class='pro_pg_sp']");
				}

				if (StringUtils.isBlank(promotionsPrice)) {
					promotionsPrice = getXPathValue(doc,
							"//DIV[@class='price_001']/B");
				}

				LOG.info("promotionsPrice:" + promotionsPrice);
				crawlDatas.setPromotionsPrice(new BigDecimal(StringUtils.replace(promotionsPrice, ",", "")));
				 
				// 商品規格
				String productSpec = getXPathValue(doc,
						"//P[@class='pro_explain']");

				if (StringUtils.isBlank(productSpec)) {

					productSpec = getXPathValue(doc,
							"//DIV[@class='pro_pg_cont_tit']");

				}

				crawlDatas.setProductSpec(productSpec);
			

				LOG.info("productSpec:" + productSpec);

				// 促銷內容

				String promotions = (getXPathValue(doc,
						"//DIV[@class='happy_e_point']"));

				if (StringUtils.isBlank(promotions)) {
					// price_003
					promotions = (getXPathValue(doc,
							"//DIV[@class='price_003']"));

				}

				crawlDatas.setPromotions(promotions);
			
				// 商品描述
				String prodDesc = (getXPathValue(doc,
						"//DIV[@class='pro_pg_cont_txt']"));

				LOG.info("prodDesc:" + prodDesc);

				
				crawlDatas.setProductDescription(prodDesc);
				saveCrawlData(url, crawlDatas);
			}
			// 用於網頁內容索引的頁面內容，一班是去頭去尾用處理後的有效訊息內容
			String txt = getXPathValue(doc, "//BODY");
			LOG.info("txt:" + txt);
			if (StringUtils.isNotBlank(txt)) {
				parse.setText(txt);
			} else {
				LOG.warn("NO data parased");
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return parse;
	}

	@Override
	public String getUrlFilterRegex() {
		return "http://*";
	}

	@Override
	protected boolean isParseDataFetchLoaded(String html) {
		return !html.contains("內容載入中");
	}

	@Override
	protected boolean isContentMatchedForParse(String url, String html) {
		return true;
	}
}
