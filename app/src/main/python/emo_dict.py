import math
import datetime
from os.path import dirname, join

fn_sentiment_pos = join(dirname(__file__), "hownet_dict/sentiment_pos.txt")
fn_sentiment_neg = join(dirname(__file__), "hownet_dict/sentiment_neg.txt")
fn_remark_pos = join(dirname(__file__), "hownet_dict/remark_pos.txt")
fn_remark_neg = join(dirname(__file__), "hownet_dict/remark_neg.txt")
fn_adverb_extreme = join(dirname(__file__), "hownet_dict/adverb_extreme.txt")
fn_adverb_insufficiently = join(dirname(__file__), "hownet_dict/adverb_insufficiently.txt")
fn_adverb_ish = join(dirname(__file__), "hownet_dict/adverb_ish.txt")
fn_adverb_more = join(dirname(__file__), "hownet_dict/adverb_more.txt")
fn_adverb_over = join(dirname(__file__), "hownet_dict/adverb_over.txt")
fn_adverb_very = join(dirname(__file__), "hownet_dict/adverb_very.txt")
fn_inverse = join(dirname(__file__), "hownet_dict/inverse.txt")
fn_cn_stopwords = join(dirname(__file__), "hownet_dict/cn_stopwords.txt")

kwd_sentiment_pos = []
kwd_sentiment_neg = []
kwd_remark_pos = []
kwd_remark_neg = []
kwd_adverb_extreme = []
kwd_adverb_insufficiently = []
kwd_adverb_ish = []
kwd_adverb_more = []
kwd_adverb_over = []
kwd_adverb_very = []
kwd_inverse = []
kwd_reverse = ["但是", "然而"]
kwd_cn_stopwords = []

def preprocess(filename):
    f = open(filename, 'r', encoding='utf-8')
    origin_words = f.read()
    origin_words = origin_words.split('\n')
    return origin_words

# 匹配程度副词
def match_adverb(word, debug=False):
    factor = 1
    reverse = 1
    if word in kwd_adverb_extreme:
        factor = 6.5
    elif word in kwd_adverb_very:
        factor = 5.2
    elif word in kwd_adverb_over:
        factor = 4.0
    elif word in kwd_adverb_ish:
        factor = 2.8
    elif word in kwd_adverb_more:
        factor = 1.6
    elif word in kwd_adverb_insufficiently:
        factor = 0.8

    if word in kwd_inverse:
        reverse = -1

    if word in kwd_reverse:
        if (factor == 0):
            factor = 2
        else:
            factor *= 1.5

    if (word == '好'):  # 对这个字进行特判
        factor = 4

    return factor, reverse

# 加上副词匹配
def find_with_adverb(wordsList, sig_score=False, debug=False):
    score = 50  # 以 50 为初始分数
    pos_count = 0
    neg_count = 0
    pos_words = []
    neg_words = []

    i = 0
    senti_i = 0

    for word in wordsList:
        tf = 0

        if (word != '好' and word[0] == '好'):
            tf += 1.75

        if word in kwd_sentiment_pos:
            pos_count += 1
            pos_words.append(word)
            for w in wordsList[senti_i:i + 1]:
                rtf, rev = match_adverb(w, debug)
                tf = (tf + rtf) * rev
            score += tf * 6.0
            senti_i = i + 1

        elif word in kwd_remark_pos:
            pos_count += 1
            pos_words.append(word)
            for w in wordsList[senti_i:i + 1]:
                rtf, rev = match_adverb(w, debug)
                tf = (tf + rtf) * rev
            score += tf * 4.0
            senti_i = i + 1

        elif word in kwd_sentiment_neg:
            neg_count += 1
            neg_words.append(word)
            for w in wordsList[senti_i:i + 1]:
                rtf, rev = match_adverb(w, debug)
                tf = (tf + rtf) * rev
            score += tf * -6.0
            senti_i = i + 1

        elif word in kwd_remark_neg:
            neg_count += 1
            neg_words.append(word)
            for w in wordsList[senti_i:i + 1]:
                rtf, rev = match_adverb(w, debug)
                tf = (tf + rtf) * rev
            score += tf * -4.0
            senti_i = i + 1

        i += 1

    if (debug is True):
        print(wordsList, "\npos_count: ", pos_count, "\nneg_count: ", neg_count)
        print("pos_words: ", pos_words)
        print("neg_words: ", neg_words)

    return score

def get_score(sentence, sig_score=False, debug=False):
    return find_with_adverb(sentence, sig_score, debug)


def ed_init():
    global kwd_sentiment_pos
    global kwd_sentiment_neg
    global kwd_remark_pos
    global kwd_remark_neg
    global kwd_adverb_extreme
    global kwd_adverb_insufficiently
    global kwd_adverb_ish
    global kwd_adverb_more
    global kwd_adverb_over
    global kwd_adverb_very
    global kwd_inverse
    global kwd_cn_stopwords

    kwd_sentiment_pos = preprocess(fn_sentiment_pos)
    kwd_sentiment_neg = preprocess(fn_sentiment_neg)
    kwd_remark_pos = preprocess(fn_remark_pos)
    kwd_remark_neg = preprocess(fn_remark_neg)
    kwd_cn_stopwords = preprocess(fn_cn_stopwords)
    kwd_adverb_extreme = preprocess(fn_adverb_extreme)
    kwd_adverb_insufficiently = preprocess(fn_adverb_insufficiently)
    kwd_adverb_ish = preprocess(fn_adverb_ish)
    kwd_adverb_more = preprocess(fn_adverb_more)
    kwd_adverb_over = preprocess(fn_adverb_over)
    kwd_adverb_very = preprocess(fn_adverb_very)
    kwd_inverse = preprocess(fn_inverse)