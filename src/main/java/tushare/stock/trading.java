package tushare.stock;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class trading {
	
	public static void main(String[] args) {
		trading trading = new trading();
		trading.get_hist_data("600789","2015-10-08","2015-10-12","D",3);
	}
	
	/**
	 * 历史行情
	 * 
	 * 获取个股历史交易数据（包括均线数据），可以通过参数设置获取日k线、周k线、月k线，以及5分钟、15分钟、30分钟和60分钟k线数据。
	 * 本接口只能获取近3年的日线数据，适合搭配均线数据进行选股和分析，如果需要全部历史数据，请调用下一个接口get_h_data()。
	 * 
	 * 调用方法：
	 * 	ts.get_hist_data('600848') #一次性获取全部日k线数据
	 * 
	 * 结果显示：
	 * 	             open    high   close     low     volume    p_change  ma5 \
		date
		2012-01-11   6.880   7.380   7.060   6.880   14129.96     2.62   7.060
		2012-01-12   7.050   7.100   6.980   6.900    7895.19    -1.13   7.020
		2012-01-13   6.950   7.000   6.700   6.690    6611.87    -4.01   6.913
		2012-01-16   6.680   6.750   6.510   6.480    2941.63    -2.84   6.813
		2012-01-17   6.660   6.880   6.860   6.460    8642.57     5.38   6.822
		2012-01-18   7.000   7.300   6.890   6.880   13075.40     0.44   6.788
		2012-01-19   6.690   6.950   6.890   6.680    6117.32     0.00   6.770
		2012-01-20   6.870   7.080   7.010   6.870    6813.09     1.74   6.832
		
		             ma10    ma20      v_ma5     v_ma10     v_ma20     turnover
		date
		2012-01-11   7.060   7.060   14129.96   14129.96   14129.96     0.48
		2012-01-12   7.020   7.020   11012.58   11012.58   11012.58     0.27
		2012-01-13   6.913   6.913    9545.67    9545.67    9545.67     0.23
		2012-01-16   6.813   6.813    7894.66    7894.66    7894.66     0.10
		2012-01-17   6.822   6.822    8044.24    8044.24    8044.24     0.30
		2012-01-18   6.833   6.833    7833.33    8882.77    8882.77     0.45
		2012-01-19   6.841   6.841    7477.76    8487.71    8487.71     0.21
		2012-01-20   6.863   6.863    7518.00    8278.38    8278.38     0.23
		
		设定历史数据的时间：
		ts.get_hist_data('600848',start='2015-01-05',end='2015-01-09')

            open    high   close     low    volume     p_change     ma5    ma10 \
date
2015-01-05  11.160  11.390  11.260  10.890  46383.57     1.26  11.156  11.212
2015-01-06  11.130  11.660  11.610  11.030  59199.93     3.11  11.182  11.155
2015-01-07  11.580  11.990  11.920  11.480  86681.38     2.67  11.366  11.251
2015-01-08  11.700  11.920  11.670  11.640  56845.71    -2.10  11.516  11.349
2015-01-09  11.680  11.710  11.230  11.190  44851.56    -3.77  11.538  11.363
            ma20     v_ma5    v_ma10     v_ma20      turnover
date
2015-01-05  11.198  58648.75  68429.87   97141.81     1.59
2015-01-06  11.382  54854.38  63401.05   98686.98     2.03
2015-01-07  11.543  55049.74  61628.07  103010.58     2.97
2015-01-08  11.647  57268.99  61376.00  105823.50     1.95
2015-01-09  11.682  58792.43  60665.93  107924.27     1.54

其他：
ts.get_hist_data('600848'，ktype='W') #获取周k线数据
ts.get_hist_data('600848'，ktype='M') #获取月k线数据
ts.get_hist_data('600848'，ktype='5') #获取5分钟k线数据
ts.get_hist_data('600848'，ktype='15') #获取15分钟k线数据
ts.get_hist_data('600848'，ktype='30') #获取30分钟k线数据
ts.get_hist_data('600848'，ktype='60') #获取60分钟k线数据
ts.get_hist_data('sh'）#获取上证指数k线数据，其它参数与个股一致，下同
ts.get_hist_data('sz'）#获取深圳成指k线数据
ts.get_hist_data('hs300'）#获取沪深300指数k线数据
ts.get_hist_data('sz50'）#获取上证50指数k线数据
ts.get_hist_data('zxb'）#获取中小板指数k线数据
ts.get_hist_data('cyb'）#获取创业板指数k线数据

	 * @param code 股票代码，即6位数字代码，或者指数代码（sh=上证指数 sz=深圳成指 hs300=沪深300指数 sz50=上证50 zxb=中小板 cyb=创业板）
	 * @param start 开始日期，格式YYYY-MM-DD
	 * @param end 结束日期，格式YYYY-MM-DD
	 * @param ktype 数据类型，D=日k线 W=周 M=月 5=5分钟 15=15分钟 30=30分钟 60=60分钟，默认为D
	 * @param retry_count 当网络异常后重试次数，默认为3
	 * 
	 * @return date：日期
open：开盘价
high：最高价
close：收盘价
low：最低价
volume：成交量
price_change：价格变动
p_change：涨跌幅
ma5：5日均价
ma10：10日均价
ma20:20日均价
v_ma5:5日均量
v_ma10:10日均量
v_ma20:20日均量
turnover:换手率[注：指数无此项]
	 */

	
	//def get_hist_data(code=None, start=None, end=None,
//  ktype='D', retry_count=3,
//  pause=0.001):'
	//"""
	//获取个股历史交易记录
	//Parameters
	//------
	//code:string
	//  股票代码 e.g. 600848
	//start:string
	//  开始日期 format：YYYY-MM-DD 为空时取到API所提供的最早日期数据
	//end:string
	//  结束日期 format：YYYY-MM-DD 为空时取到最近一个交易日数据
	//ktype：string
	//  数据类型，D=日k线 W=周 M=月 5=5分钟 15=15分钟 30=30分钟 60=60分钟，默认为D
	//retry_count : int, 默认 3
	// 如遇网络等问题重复执行的次数 
	//pause : int, 默认 0
	//重复请求数据过程中暂停的秒数，防止请求间隔时间太短出现的问题
	//return
	//-------
	//DataFrame
	//属性:日期 ，开盘价， 最高价， 收盘价， 最低价， 成交量， 价格变动 ，涨跌幅，5日均价，10日均价，20日均价，5日均量，10日均量，20日均量，换手率
	//"""
	public void get_hist_data(String code,String start, String end,
		String  ktype, int retry_count
		) {
		if(ktype == null) {
			ktype ="D";
		}
		if(retry_count == -1) {
			retry_count =3;
		}
		
		
String symbol = _code_to_symbol(code);
String url = "";
if(cons.K_LABELS.contains(ktype.toUpperCase())) {
	url = String.format(cons.DAY_PRICE_URL, cons.P_TYPE.get("http"), cons.DOMAINS.get("ifeng"),
			cons.K_TYPE.get(ktype.toUpperCase()), symbol);
} else if(cons.K_MIN_LABELS.contains(ktype)) {
	url = String.format(cons.DAY_PRICE_MIN_URL, cons.P_TYPE.get("http"), cons.DOMAINS.get("ifeng"),
          symbol, ktype);
} else {
	throw new RuntimeException("ktype input error.");
}


String lines = request(url, retry_count);
if(lines.length()< 15) {// #no data
	return;
} 
	


//js = json.loads(lines.decode('utf-8') if ct.PY3 else lines)
List<String>cols =new ArrayList<>();
if(cons.INDEX_LABELS.contains(code) && cons.K_LABELS.contains(ktype.toUpperCase())) {
	cols = cons.INX_DAY_PRICE_COLUMNS;
} else {
	cols = cons.DAY_PRICE_COLUMNS;
}

JSONObject js = JSON.parseObject(lines);
int len = js.getJSONArray("record").getJSONArray(0).size();
System.out.println(len);
if(len == 14) {
	cols = cons.INX_DAY_PRICE_COLUMNS;
}
//df = pd.DataFrame(js['record'], columns=cols)
//if ktype.upper() in ['D', 'W', 'M']:
//    df = df.applymap(lambda x: x.replace(u',', u''))
//for col in cols[1:]:
//    df[col] = df[col].astype(float)
//if start is not None:
//    df = df[df.date >= start]
//if end is not None:
//    df = df[df.date <= end]
//if (code in ct.INDEX_LABELS) & (ktype in ct.K_MIN_LABELS):
//    df = df.drop('turnover', axis=1)
//df = df.set_index('date')
//return df
	}
	
	 /** 
     * 发送 get请求 
     */  
    public String request(String url, int retry_count) {  
    	
    	HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

    	    public boolean retryRequest(
    	            IOException exception,
    	            int executionCount,
    	            HttpContext context) {
    	        if (executionCount >= retry_count) {
    	            // Do not retry if over max retry count
    	            return false;
    	        }
    	        if (exception instanceof InterruptedIOException) {
    	            // Timeout
    	            return false;
    	        }
    	        if (exception instanceof UnknownHostException) {
    	            // Unknown host
    	            return false;
    	        }
    	        if (exception instanceof ConnectTimeoutException) {
    	            // Connection refused
    	            return false;
    	        }
    	        if (exception instanceof SSLException) {
    	            // SSL handshake exception
    	            return false;
    	        }
    	        HttpClientContext clientContext = HttpClientContext.adapt(context);
    	        HttpRequest request = clientContext.getRequest();
    	        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
    	        if (idempotent) {
    	            // Retry if the request is considered idempotent
    	            return true;
    	        }
    	        return false;
    	    }

    	};
    	CloseableHttpClient httpclient = HttpClients.custom()
    	        .setRetryHandler(myRetryHandler)
    	        .build();
    	
//        CloseableHttpClient httpclient = HttpClients.createDefault();  
        try {  
            // 创建httpget.    
            HttpGet httpget = new HttpGet(url);  
            System.out.println("executing request " + httpget.getURI());  
            // 执行get请求.    
            CloseableHttpResponse response = httpclient.execute(httpget);  
            try {  
                // 获取响应实体    
                HttpEntity entity = response.getEntity();  
                System.out.println("--------------------------------------");  
                // 打印响应状态    
                System.out.println(response.getStatusLine());
                
                String responseText = "";
                
                int i = 0;
                if (entity != null) {  
                    // 打印响应内容长度    
                    System.out.println("Response content length: " + entity.getContentLength());  
                    // 打印响应内容    
                    responseText = EntityUtils.toString(entity);
                    System.out.println((i++)+"d Response content: " + responseText); 
                }  
                System.out.println("------------------------------------");  
                
                return responseText;
            } finally {  
                response.close();  
            }  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (ParseException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        
        return "";
    }  



//# -*- coding:utf-8 -*- 
//"""
//交易数据接口 
//Created on 2014/07/31
//@author: Jimmy Liu
//@group : waditu
//@contact: jimmysoa@sina.cn
//"""
//from __future__ import division
//
//import time
//import json
//import lxml.html
//from lxml import etree
//import pandas as pd
//import numpy as np
//from tushare.stock import cons as ct
//import re
//from pandas.compat import StringIO
//from tushare.util import dateu as du
//try:
//    from urllib.request import urlopen, Request
//except ImportError:
//    from urllib2 import urlopen, Request
//
//

//
//def _parsing_dayprice_json(pageNum=1):
//    """
//           处理当日行情分页数据，格式为json
//     Parameters
//     ------
//        pageNum:页码
//     return
//     -------
//        DataFrame 当日所有股票交易数据(DataFrame)
//    """
//    ct._write_console()
//    request = Request(ct.SINA_DAY_PRICE_URL%(ct.P_TYPE['http'], ct.DOMAINS['vsf'],
//                                 ct.PAGES['jv'], pageNum))
//    text = urlopen(request, timeout=10).read()
//    if text == 'null':
//        return None
//    reg = re.compile(r'\,(.*?)\:') 
//    text = reg.sub(r',"\1":', text.decode('gbk') if ct.PY3 else text) 
//    text = text.replace('"{symbol', '{"symbol')
//    text = text.replace('{symbol', '{"symbol"')
//    if ct.PY3:
//        jstr = json.dumps(text)
//    else:
//        jstr = json.dumps(text, encoding='GBK')
//    js = json.loads(jstr)
//    df = pd.DataFrame(pd.read_json(js, dtype={'code':object}),
//                      columns=ct.DAY_TRADING_COLUMNS)
//    df = df.drop('symbol', axis=1)
//    df = df.ix[df.volume > 0]
//    return df
//
//
//def get_tick_data(code=None, date=None, retry_count=3, pause=0.001):
//    """
//        获取分笔数据
//    Parameters
//    ------
//        code:string
//                  股票代码 e.g. 600848
//        date:string
//                  日期 format：YYYY-MM-DD
//        retry_count : int, 默认 3
//                  如遇网络等问题重复执行的次数
//        pause : int, 默认 0
//                 重复请求数据过程中暂停的秒数，防止请求间隔时间太短出现的问题
//     return
//     -------
//        DataFrame 当日所有股票交易数据(DataFrame)
//              属性:成交时间、成交价格、价格变动，成交手、成交金额(元)，买卖类型
//    """
//    if code is None or len(code)!=6 or date is None:
//        return None
//    symbol = _code_to_symbol(code)
//    for _ in range(retry_count):
//        time.sleep(pause)
//        try:
//            re = Request(ct.TICK_PRICE_URL % (ct.P_TYPE['http'], ct.DOMAINS['sf'], ct.PAGES['dl'],
//                                date, symbol))
//            lines = urlopen(re, timeout=10).read()
//            lines = lines.decode('GBK') 
//            if len(lines) < 100:
//                return None
//            df = pd.read_table(StringIO(lines), names=ct.TICK_COLUMNS,
//                               skiprows=[0])      
//        except Exception as e:
//            print(e)
//        else:
//            return df
//    raise IOError(ct.NETWORK_URL_ERROR_MSG)
//
//
//def get_today_ticks(code=None, retry_count=3, pause=0.001):
//    """
//        获取当日分笔明细数据
//    Parameters
//    ------
//        code:string
//                  股票代码 e.g. 600848
//        retry_count : int, 默认 3
//                  如遇网络等问题重复执行的次数
//        pause : int, 默认 0
//                 重复请求数据过程中暂停的秒数，防止请求间隔时间太短出现的问题
//     return
//     -------
//        DataFrame 当日所有股票交易数据(DataFrame)
//              属性:成交时间、成交价格、价格变动，成交手、成交金额(元)，买卖类型
//    """
//    if code is None or len(code)!=6 :
//        return None
//    symbol = _code_to_symbol(code)
//    date = du.today()
//    try:
//        request = Request(ct.TODAY_TICKS_PAGE_URL % (ct.P_TYPE['http'], ct.DOMAINS['vsf'],
//                                                   ct.PAGES['jv'], date,
//                                                   symbol))
//        data_str = urlopen(request, timeout=10).read()
//        data_str = data_str.decode('GBK')
//        data_str = data_str[1:-1]
//        data_str = eval(data_str, type('Dummy', (dict,), 
//                                       dict(__getitem__ = lambda s, n:n))())
//        data_str = json.dumps(data_str)
//        data_str = json.loads(data_str)
//        pages = len(data_str['detailPages'])
//        data = pd.DataFrame()
//        ct._write_head()
//        for pNo in range(1, pages):
//            data = data.append(_today_ticks(symbol, date, pNo,
//                                            retry_count, pause), ignore_index=True)
//    except Exception as er:
//        print(str(er))
//    return data
//
//
//def _today_ticks(symbol, tdate, pageNo, retry_count, pause):
//    ct._write_console()
//    for _ in range(retry_count):
//        time.sleep(pause)
//        try:
//            html = lxml.html.parse(ct.TODAY_TICKS_URL % (ct.P_TYPE['http'],
//                                                         ct.DOMAINS['vsf'], ct.PAGES['t_ticks'],
//                                                         symbol, tdate, pageNo
//                                ))  
//            res = html.xpath('//table[@id=\"datatbl\"]/tbody/tr')
//            if ct.PY3:
//                sarr = [etree.tostring(node).decode('utf-8') for node in res]
//            else:
//                sarr = [etree.tostring(node) for node in res]
//            sarr = ''.join(sarr)
//            sarr = '<table>%s</table>'%sarr
//            sarr = sarr.replace('--', '0')
//            df = pd.read_html(StringIO(sarr), parse_dates=False)[0]
//            df.columns = ct.TODAY_TICK_COLUMNS
//            df['pchange'] = df['pchange'].map(lambda x : x.replace('%', ''))
//        except Exception as e:
//            print(e)
//        else:
//            return df
//    raise IOError(ct.NETWORK_URL_ERROR_MSG)
//        
//    
//def get_today_all():
//    """
//        一次性获取最近一个日交易日所有股票的交易数据
//    return
//    -------
//      DataFrame
//           属性：代码，名称，涨跌幅，现价，开盘价，最高价，最低价，最日收盘价，成交量，换手率
//    """
//    ct._write_head()
//    df = _parsing_dayprice_json(1)
//    if df is not None:
//        for i in range(2, ct.PAGE_NUM[0]):
//            newdf = _parsing_dayprice_json(i)
//            df = df.append(newdf, ignore_index=True)
//    return df
//
//
//def get_realtime_quotes(symbols=None):
//    """
//        获取实时交易数据 getting real time quotes data
//       用于跟踪交易情况（本次执行的结果-上一次执行的数据）
//    Parameters
//    ------
//        symbols : string, array-like object (list, tuple, Series).
//        
//    return
//    -------
//        DataFrame 实时交易数据
//              属性:0：name，股票名字
//            1：open，今日开盘价
//            2：pre_close，昨日收盘价
//            3：price，当前价格
//            4：high，今日最高价
//            5：low，今日最低价
//            6：bid，竞买价，即“买一”报价
//            7：ask，竞卖价，即“卖一”报价
//            8：volumn，成交量 maybe you need do volumn/100
//            9：amount，成交金额（元 CNY）
//            10：b1_v，委买一（笔数 bid volume）
//            11：b1_p，委买一（价格 bid price）
//            12：b2_v，“买二”
//            13：b2_p，“买二”
//            14：b3_v，“买三”
//            15：b3_p，“买三”
//            16：b4_v，“买四”
//            17：b4_p，“买四”
//            18：b5_v，“买五”
//            19：b5_p，“买五”
//            20：a1_v，委卖一（笔数 ask volume）
//            21：a1_p，委卖一（价格 ask price）
//            ...
//            30：date，日期；
//            31：time，时间；
//    """
//    symbols_list = ''
//    if isinstance(symbols, list) or isinstance(symbols, set) or isinstance(symbols, tuple) or isinstance(symbols, pd.Series):
//        for code in symbols:
//            symbols_list += _code_to_symbol(code) + ','
//    else:
//        symbols_list = _code_to_symbol(symbols)
//        
//    symbols_list = symbols_list[:-1] if len(symbols_list) > 8 else symbols_list 
//    request = Request(ct.LIVE_DATA_URL%(ct.P_TYPE['http'], ct.DOMAINS['sinahq'],
//                                                _random(), symbols_list))
//    text = urlopen(request,timeout=10).read()
//    text = text.decode('GBK')
//    reg = re.compile(r'\="(.*?)\";')
//    data = reg.findall(text)
//    regSym = re.compile(r'(?:sh|sz)(.*?)\=')
//    syms = regSym.findall(text)
//    data_list = []
//    syms_list = []
//    for index, row in enumerate(data):
//        if len(row)>1:
//            data_list.append([astr for astr in row.split(',')])
//            syms_list.append(syms[index])
//    if len(syms_list) == 0:
//        return None
//    df = pd.DataFrame(data_list, columns=ct.LIVE_DATA_COLS)
//    df = df.drop('s', axis=1)
//    df['code'] = syms_list
//    ls = [cls for cls in df.columns if '_v' in cls]
//    for txt in ls:
//        df[txt] = df[txt].map(lambda x : x[:-2])
//    return df
//
//
//def get_h_data(code, start=None, end=None, autype='qfq',
//               index=False, retry_count=3, pause=0.001):
//    '''
//    获取历史复权数据
//    Parameters
//    ------
//      code:string
//                  股票代码 e.g. 600848
//      start:string
//                  开始日期 format：YYYY-MM-DD 为空时取当前日期
//      end:string
//                  结束日期 format：YYYY-MM-DD 为空时取去年今日
//      autype:string
//                  复权类型，qfq-前复权 hfq-后复权 None-不复权，默认为qfq
//      retry_count : int, 默认 3
//                 如遇网络等问题重复执行的次数 
//      pause : int, 默认 0
//                重复请求数据过程中暂停的秒数，防止请求间隔时间太短出现的问题
//    return
//    -------
//      DataFrame
//          date 交易日期 (index)
//          open 开盘价
//          high  最高价
//          close 收盘价
//          low 最低价
//          volume 成交量
//          amount 成交金额
//    '''
//    
//    start = du.today_last_year() if start is None else start
//    end = du.today() if end is None else end
//    qs = du.get_quarts(start, end)
//    qt = qs[0]
//    ct._write_head()
//    data = _parse_fq_data(_get_index_url(index, code, qt), index,
//                          retry_count, pause)
//    if len(qs)>1:
//        for d in range(1, len(qs)):
//            qt = qs[d]
//            ct._write_console()
//            df = _parse_fq_data(_get_index_url(index, code, qt), index,
//                                retry_count, pause)
//            data = data.append(df, ignore_index=True)
//    if len(data) == 0 or len(data[(data.date>=start)&(data.date<=end)]) == 0:
//        return None
//    data = data.drop_duplicates('date')
//    if index:
//        data = data[(data.date>=start) & (data.date<=end)]
//        data = data.set_index('date')
//        data = data.sort_index(ascending=False)
//        return data
//    if autype == 'hfq':
//        data = data.drop('factor', axis=1)
//        data = data[(data.date>=start) & (data.date<=end)]
//        for label in ['open', 'high', 'close', 'low']:
//            data[label] = data[label].map(ct.FORMAT)
//            data[label] = data[label].astype(float)
//        data = data.set_index('date')
//        data = data.sort_index(ascending = False)
//        return data
//    else:
//        if autype == 'qfq':
//            data = data.drop('factor', axis=1)
//            df = _parase_fq_factor(code, start, end)
//            df = df.drop_duplicates('date')
//            df = df.sort('date', ascending=False)
//            frow = df.head(1)
//            rt = get_realtime_quotes(code)
//            if rt is None:
//                return None
//            if ((float(rt['high']) == 0) & (float(rt['low']) == 0)):
//                preClose = float(rt['pre_close'])
//            else:
//                if du.is_holiday(du.today()):
//                    preClose = float(rt['price'])
//                else:
//                    if (du.get_hour() > 9) & (du.get_hour() < 18):
//                        preClose = float(rt['pre_close'])
//                    else:
//                        preClose = float(rt['price'])
//            
//            rate = float(frow['factor']) / preClose
//            data = data[(data.date >= start) & (data.date <= end)]
//            for label in ['open', 'high', 'low', 'close']:
//                data[label] = data[label] / rate
//                data[label] = data[label].map(ct.FORMAT)
//                data[label] = data[label].astype(float)
//            data = data.set_index('date')
//            data = data.sort_index(ascending = False)
//            return data
//        else:
//            for label in ['open', 'high', 'close', 'low']:
//                data[label] = data[label] / data['factor']
//            data = data.drop('factor', axis=1)
//            data = data[(data.date>=start) & (data.date<=end)]
//            for label in ['open', 'high', 'close', 'low']:
//                data[label] = data[label].map(ct.FORMAT)
//            data = data.set_index('date')
//            data = data.sort_index(ascending=False)
//            data = data.astype(float)
//            return data
//
//
//def _parase_fq_factor(code, start, end):
//    symbol = _code_to_symbol(code)
//    request = Request(ct.HIST_FQ_FACTOR_URL%(ct.P_TYPE['http'],
//                                             ct.DOMAINS['vsf'], symbol))
//    text = urlopen(request, timeout=10).read()
//    text = text[1:len(text)-1]
//    text = text.decode('utf-8') if ct.PY3 else text
//    text = text.replace('{_', '{"')
//    text = text.replace('total', '"total"')
//    text = text.replace('data', '"data"')
//    text = text.replace(':"', '":"')
//    text = text.replace('",_', '","')
//    text = text.replace('_', '-')
//    text = json.loads(text)
//    df = pd.DataFrame({'date':list(text['data'].keys()), 'factor':list(text['data'].values())})
//    df['date'] = df['date'].map(_fun_except) # for null case
//    if df['date'].dtypes == np.object:
//        df['date'] = df['date'].astype(np.datetime64)
//    df = df.drop_duplicates('date')
//    df['factor'] = df['factor'].astype(float)
//    return df
//
//
//def _fun_except(x):
//    if len(x) > 10:
//        return x[-10:]
//    else:
//        return x
//
//
//def _parse_fq_data(url, index, retry_count, pause):
//    for _ in range(retry_count):
//        time.sleep(pause)
//        try:
//            request = Request(url)
//            text = urlopen(request, timeout=10).read()
//            text = text.decode('GBK')
//            html = lxml.html.parse(StringIO(text))
//            res = html.xpath('//table[@id=\"FundHoldSharesTable\"]')
//            if ct.PY3:
//                sarr = [etree.tostring(node).decode('utf-8') for node in res]
//            else:
//                sarr = [etree.tostring(node) for node in res]
//            sarr = ''.join(sarr)
//            df = pd.read_html(sarr, skiprows = [0, 1])[0]
//            if len(df) == 0:
//                return pd.DataFrame()
//            if index:
//                df.columns = ct.HIST_FQ_COLS[0:7]
//            else:
//                df.columns = ct.HIST_FQ_COLS
//            if df['date'].dtypes == np.object:
//                df['date'] = df['date'].astype(np.datetime64)
//            df = df.drop_duplicates('date')
//        except Exception as e:
//            print(e)
//        else:
//            return df
//    raise IOError(ct.NETWORK_URL_ERROR_MSG)
//
//
//def get_index():
//    """
//    获取大盘指数行情
//    return
//    -------
//      DataFrame
//          code:指数代码
//          name:指数名称
//          change:涨跌幅
//          open:开盘价
//          preclose:昨日收盘价
//          close:收盘价
//          high:最高价
//          low:最低价
//          volume:成交量(手)
//          amount:成交金额（亿元）
//    """
//    request = Request(ct.INDEX_HQ_URL%(ct.P_TYPE['http'],
//                                             ct.DOMAINS['sinahq']))
//    text = urlopen(request, timeout=10).read()
//    text = text.decode('GBK')
//    text = text.replace('var hq_str_sh', '').replace('var hq_str_sz', '')
//    text = text.replace('";', '').replace('"', '').replace('=', ',')
//    text = '%s%s'%(ct.INDEX_HEADER, text)
//    df = pd.read_csv(StringIO(text), sep=',', thousands=',')
//    df['change'] = (df['close'] / df['preclose'] - 1 ) * 100
//    df['amount'] = df['amount'] / 100000000
//    df['change'] = df['change'].map(ct.FORMAT)
//    df['amount'] = df['amount'].map(ct.FORMAT)
//    df = df[ct.INDEX_COLS]
//    df['code'] = df['code'].map(lambda x:str(x).zfill(6))
//    df['change'] = df['change'].astype(float)
//    df['amount'] = df['amount'].astype(float)
//    return df
// 
//
//def _get_index_url(index, code, qt):
//    if index:
//        url = ct.HIST_INDEX_URL%(ct.P_TYPE['http'], ct.DOMAINS['vsf'],
//                              code, qt[0], qt[1])
//    else:
//        url = ct.HIST_FQ_URL%(ct.P_TYPE['http'], ct.DOMAINS['vsf'],
//                              code, qt[0], qt[1])
//    return url
//
//
//def get_hists(symbols, start=None, end=None,
//                  ktype='D', retry_count=3,
//                  pause=0.001):
//    """
//    批量获取历史行情数据，具体参数和返回数据类型请参考get_hist_data接口
//    """
//    df = pd.DataFrame()
//    if isinstance(symbols, list) or isinstance(symbols, set) or isinstance(symbols, tuple) or isinstance(symbols, pd.Series):
//        for symbol in symbols:
//            data = get_hist_data(symbol, start=start, end=end,
//                                 ktype=ktype, retry_count=retry_count,
//                                 pause=pause)
//            data['code'] = symbol
//            df = df.append(data, ignore_index=True)
//        return df
//    else:
//        return None
//    
//def _random(n=13):
//    from random import randint
//    start = 10**(n-1)
//    end = (10**n)-1
//    return str(randint(start, end))
//
//
  //生成symbol代码标志
  	public String _code_to_symbol(String code) {
  		if(cons.INDEX_LABELS.contains(code)) {
  			return cons.INDEX_LIST.get(code);
  		} else {
  			if(code.length() != 6) {
  				return "";
  			} else {
  				if(code.startsWith("5") || code.startsWith("6")) {
  					return "sh" + code;
  				} else {
  					return "sz" + code;
  				}
  			}
  		}
  	}
}