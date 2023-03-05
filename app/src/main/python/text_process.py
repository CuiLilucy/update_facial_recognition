from ptokenize import eliminate_stopwords, tokenize_passage, tokenize_sentence
from emo_dict import ed_init, get_score, kwd_cn_stopwords 
from os.path import dirname, join
import jieba

import csv

# define
fileName = "word2idx.csv"
word2idx = {}

def in_text(sentence: str) -> float:
    words = eliminate_stopwords(tokenize_sentence(sentence), kwd_cn_stopwords)
    # 再简单预处理
    words = ['哈哈' if len(i) > 2 and i[0] == '哈' and i[1] == '哈' else i for i in words]

    return get_score(words, sig_score=False, debug=True)

def w2i(wordList: list) -> list:
    indexList = []
    for word in wordList:
        if word2idx.get(word) is None:
            indexList.append(0)
        else:
            indexList.append(int(word2idx.get(word)))

    if len(indexList) > 30:
        indexList = indexList[:30]
    else:
        indexList = indexList + [0] * (30 - len(indexList))
    return indexList

jieba.load_userdict(join(dirname(__file__), "hownet_dict/custom_dict.txt"))
csv_file = open(join(dirname(__file__), fileName), 'r', encoding='utf-8')
reader = csv.reader(csv_file)
word2idx = dict(reader)
print("EDINIT BEGIN")
ed_init()
print("EDINIT END")