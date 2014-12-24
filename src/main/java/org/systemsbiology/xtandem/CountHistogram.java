package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.count_mhistogram
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
class CountHistogram
{
    private int m_lLength;
    int m_pList[] = new int[ 32]; // the histogram array

   public CountHistogram( )

    {
      }

/*
 * simple copy method using the = operator
 */
    public CountHistogram(final CountHistogram rhs)

    {
        m_lLength = rhs.m_lLength;
         int a = 0;
        while (a < m_lLength) {
            m_pList[a] = rhs.m_pList[a];
            a++;
        }
     }



    float convert(final float _f)
    {
        return _f;
    }

    long add(final int _c)
    {
        if (_c < m_lLength && _c > -1) {
            m_pList[_c]++;
        }
        else if (_c > -1) {
            m_pList[m_lLength - 1]++;
        }
        else if (_c < 0) {
            m_pList[0]++;
        }
        return _c;
    }
/*
 * reset zeros the histogram array m_pList
 */

    void clear()
    {
        int a = 0;
        while (a < m_lLength) {
            m_pList[a] = 0;
            a++;
        }
    }
    CountHistogram add(final CountHistogram rhs)

    {
        m_lLength = rhs.m_lLength;
        int a = 0;
        while (a < m_lLength) {
            m_pList[a] += rhs.m_pList[a];
            a++;
        }
        return this;
    }
/*
 * list returns a specified value from the stochastic distribution of the histogram
 */

    long list(int _l)
    {
        return m_pList[_l];
    }
/*
 * length returns the maximum length of the histogram
 */

    long length()
    {
        return m_lLength;
    }


}
