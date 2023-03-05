import re
import jieba

def eliminate_stopwords(sentence, stop_words):
    new_words = []
    for word in sentence:
        if word not in stop_words:
            new_words.append(word)
    return new_words

def tokenize_passage(passage):
    passage = re.sub('！+', '！', passage)

    passage = re.sub('([。！？\?])([^”’])', r"\1\n\2", passage)  # 单字符断句符
    passage = re.sub('(\.{6})([^”’])', r"\1\n\2", passage)  # 英文省略号
    passage = re.sub('(\…{2})([^”’])', r"\1\n\2", passage)  # 中文省略号
    passage = re.sub('([。！？\?][”’])([^，。！？\?])', r'\1\n\2', passage)
    # 如果双引号前有终止符，那么双引号才是句子的终点，把分句符\n放到双引号后，注意前面的几句都小心保留了双引号
    passage = passage.rstrip()
    passage = passage.split('\n')
    len_list = [len(len_sen) for len_sen in passage]
    # 很多规则中会考虑分号，但是这里我把它忽略不计，破折号、英文双引号等同样忽略，需要的再做些简单调整即可。
    return (passage, len_list)

def tokenize_sentence(sentence, allow_symbol=True):
    filters = ['！', '？', '!', '\?',
               '"', '#', '$', '%', '&', '\(', '\)', '\*', '\+', ',', '-', '\.', '/', ':', ';', '<', '=', '>',
               '@', '\[', '\\', '\]', '^', '_', '`', '\{', '\|', '\}', '~', '\t', '\n', '\x97', '\x96', '”', '“',
               '\【', '\】', '\（', '\、', '。', '\）',  '\，', '[\s0-9a-zA-Z]']

    sentence = re.sub("|".join(filters), "", sentence)
    sentence = jieba.cut(sentence)
    return list(sentence)
