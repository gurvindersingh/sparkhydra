package org.systemsbiology.xtandem;

/**
* org.systemsbiology.xtandem.ScoreAP
 * @author Steve Lewis
* @date Dec 21, 2010
*/
public class ScoreAP {
    public static ScoreAP[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ScoreAP.class;

    private int m_iStart;
    private int m_iEnd;
    private int m_iPos;
    private String m_strId;
//    private map<String, multimap<int, prSap>> m_mapSap;
//    private multimap<int, prSap>::
//    iterator m_itSap;
//    private multimap<int, prSap>::
//    iterator m_itSapEnd;
//    private map<String, multimap<int, prSap>>::
//    iterator m_itAcc;
    private final StringBuffer m_pSeqTrue = new StringBuffer();
    private int m_tSeqTrue;
    private int m_tLength;
    private float m_fSeqTrue;
    private int m_tCount;
    private int m_tPos;
    private char m_cCurrent;
    private boolean m_bOk;
    private boolean m_bEnd;

    public ScoreAP() {
    }

    public boolean set(String _s, final boolean _b) {
        m_bOk = false;
        if (!_b) {
            return m_bOk;
        }
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        if (m_mapSap.i()) {
//            return m_bOk;
//        }
//        m_tCount = 0;
//        m_itAcc = m_mapSap.find(_s);
//        m_bOk = true;
//        if (m_itAcc == m_mapSap.end()) {
//            m_bOk = false;
//        }
//        m_bEnd = true;
//        m_iStart = 0;
//        m_iEnd = 0;
//        return m_bOk;
    }

    public boolean initialize(String _p, final int _s, final float _f, final int _i) {
        if (!m_bOk) {
            return false;
        }
        if (m_tSeqTrue < _s) {
            m_tSeqTrue = _s;

        }
        m_pSeqTrue.setLength(0);
        m_pSeqTrue.append(_p);
        m_fSeqTrue = _f;
        m_tLength = _p.length();
        m_cCurrent = '\0';
        m_iStart = _i + 1;
        m_iEnd = m_iStart + (int) m_tLength - 1;
        m_tCount = 0;
        m_tPos = 0;
        m_strId = "";
        m_bEnd = false;
        m_iPos = m_iStart;
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        m_itSap = m_itAcc - > second.lower_bound(m_iPos);
//        m_itSapEnd = m_itAcc - > second.end();
//        if (m_itSap == m_itSapEnd) {
//            m_bEnd = true;
//            return false;
//        }
//
//        if (m_itSap - > first > m_iEnd) {
//
//            m_bEnd = true;
//            return false;
//        }
//        return true;
    }

    boolean initialize(final String _p, final int _s, final float _f) {
        if (!m_bOk) {
            return false;
        }
        if (m_tSeqTrue < _s) {
            m_tSeqTrue = _s;
        }
        m_pSeqTrue.setLength(0);
        m_pSeqTrue.append(_p);
        m_fSeqTrue = _f;
        m_pSeqTrue.setLength(0);
        m_pSeqTrue.append(_p);
        m_cCurrent = '\0';
        m_iEnd = m_iStart + (int) m_tLength - 1;
        m_tCount = 0;
        m_tPos = 0;
        m_strId = "";
        m_bEnd = false;
        m_iPos = m_iStart;
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        m_itSap = m_itAcc - > second.lower_bound(m_iPos);
//        m_itSapEnd = m_itAcc - > second.end();
//        if (m_itSap == m_itSapEnd) {
//            m_bEnd = true;
//            return false;
//        }
//
//        if (m_itSap - > first > m_iEnd) {
//
//            m_bEnd = true;
//            return false;
//        }
//        return true;
    }

    boolean next() {
        if (m_bEnd || !m_bOk) {
            return false;
        }
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        m_iPos = m_itSap - > first;
//        if (m_iPos > m_iEnd) {
//            m_bEnd = true;
//            return false;
//        }
//        m_cCurrent = m_itSap - > second.first;
//        m_tPos = m_iPos - m_iStart;
//        m_strId = m_itSap - > second.second;
//        m_tCount++;
//        m_itSap++;
//        if (m_itSap == m_itSapEnd || m_itSap - > first > m_iEnd) {
//            m_bEnd = true;
//        }
//        return true;
    }

    public int getiStart()
    {
        return m_iStart;
    }

    public void setiStart(int pIStart)
    {
        m_iStart = pIStart;
    }

    public int getiEnd()
    {
        return m_iEnd;
    }

    public void setiEnd(int pIEnd)
    {
        m_iEnd = pIEnd;
    }

    public int getiPos()
    {
        return m_iPos;
    }

    public void setiPos(int pIPos)
    {
        m_iPos = pIPos;
    }

    public String getStrId()
    {
        return m_strId;
    }

    public void setStrId(String pStrId)
    {
        m_strId = pStrId;
    }

    public int gettSeqTrue()
    {
        return m_tSeqTrue;
    }

    public void settSeqTrue(int pTSeqTrue)
    {
        m_tSeqTrue = pTSeqTrue;
    }

    public int gettLength()
    {
        return m_tLength;
    }

    public void settLength(int pTLength)
    {
        m_tLength = pTLength;
    }

    public float getfSeqTrue()
    {
        return m_fSeqTrue;
    }

    public void setfSeqTrue(float pFSeqTrue)
    {
        m_fSeqTrue = pFSeqTrue;
    }

    public int gettCount()
    {
        return m_tCount;
    }

    public void settCount(int pTCount)
    {
        m_tCount = pTCount;
    }

    public int gettPos()
    {
        return m_tPos;
    }

    public void settPos(int pTPos)
    {
        m_tPos = pTPos;
    }

    public char getcCurrent()
    {
        return m_cCurrent;
    }

    public void setcCurrent(char pCCurrent)
    {
        m_cCurrent = pCCurrent;
    }

    public boolean isbOk()
    {
        return m_bOk;
    }

    public void setbOk(boolean pBOk)
    {
        m_bOk = pBOk;
    }

    public boolean isbEnd()
    {
        return m_bEnd;
    }

    public void setbEnd(boolean pBEnd)
    {
        m_bEnd = pBEnd;
    }
}
