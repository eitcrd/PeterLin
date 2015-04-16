package org.apache.nutch.parse.s2jh;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import org.w3c.dom.Node;

public class PchomeHtmlParseFilter extends AbstractHtmlParseFilter {

	public static final Logger LOG = LoggerFactory
			.getLogger(PchomeHtmlParseFilter.class);

	@Override
	public Parse filterInternal(String url, WebPage page, Parse parse,
			HTMLMetaTags metaTags, DocumentFragment doc) {

		LOG.info("PchomeHtmlParseFilter begin.....");

		try {

			String entry = getXPathValue(doc, "//DL[@id='nc17Container']");
			if (StringUtils.isBlank(entry)) {
				entry = getXPathValue(doc, "//DL[@class='prod_list']");
			}
			if (StringUtils.isNotBlank(entry)) {
				// 開始擷取所需資料

				CrawlData crawlDatas = new CrawlData();

				String title = getXPathValue(doc, "//P[@id='NickContainer']");

				crawlDatas.setTitle(StringUtils.replace(title, " ", ""));

				// 建議市價
				String marketPrice = getXPathValue(doc,
						"//DIV[@id='PaymentContainer']/H6/STRONG/I");

				LOG.info("marketPrice:" + marketPrice);
				if (StringUtils.isNotBlank(marketPrice)) {
					crawlDatas.setPrice(new BigDecimal(StringUtils.replace(
							marketPrice, ",", "")));
				}
				// 銷售價
				String promotionsPrice = getXPathValue(doc,
						"//DIV[@id='PaymentContainer']/DIV/H6/STRONG/I");

				LOG.info("promotionsPrice:" + promotionsPrice);
				
				if (StringUtils.isNotBlank(promotionsPrice)) {
					crawlDatas.setPromotionsPrice(new BigDecimal(StringUtils
							.replace(promotionsPrice, ",", "")));
				}
				// 商品規格
				String productSpec = getXPathValue(doc, "//DD[@id='Stmthtml']");

				LOG.info("@commoditySpec:" + productSpec);

				crawlDatas.setProductSpec(StringUtils.replace(productSpec, " ", ""));

				LOG.info("productSpec:" + productSpec);

				// 促銷內容

				String promotions = (getXPathValue(doc,
						"//DIV[@class='bar_event']"));

				String promotionsArr[] = new String[2];
				String promotionsLabel = "";
				String promotionsVal = "";
				String promotionMark = "：";
				LOG.info("promotions:" + promotions);

				if (StringUtils.isNotBlank(promotions)) {
					
					if (StringUtils.containsAny(promotions, promotionMark)) {
						promotionsArr = promotions.split(promotionMark);
						promotionsLabel = promotionsArr[0].trim();
						promotionsVal = promotionsArr[1].trim();
					}

				}

				LOG.info("promotionsall:" + promotionsLabel + promotionsVal);
				crawlDatas.setPromotions(promotions);
				// 商品描述
				String prodDesc = (getXPathValue(doc,
						"//DIV[@id='SloganContainer']"));

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

	// public static void main(String[] args) {
	// System.out.println("####PchomeHtmlParseFilter");
	// }
}
