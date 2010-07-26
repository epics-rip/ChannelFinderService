import sys, random

"""
Usage
python create_bench_db.py
"""

max_prop = 40           # must be >=20
max_tag  = 60           # must be >=11

my_version = "0.2"
cowner = "testc"
powner = "testp"
towner = "testt"
chan_id = 0
prop_id = 0

chan_in_cell = 0
tokens = []
val_bucket = ['0','1','2','5','10','20','50','100','200','500']

def fail(emesg, retcode):
    print >>sys.stderr, "%s: ERROR: %s" % (sys.argv[0],emesg)
    sys.exit(retcode)

def create_db():
    print "USE cf_bench_xl;"

    print "DROP TABLE IF EXISTS property;"
    print "DROP TABLE IF EXISTS channel;"

    print "CREATE TABLE  `channel` ("
    print "  `id` int(10) unsigned NOT NULL auto_increment,"
    print "  `name` varchar(45) NOT NULL,"
    print "  `owner` varchar(45) NOT NULL,"
    print "  PRIMARY KEY  (`id`),"
    print "  KEY `channel_name` (`name`)"
    print ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"

    print "CREATE TABLE  `property` ("
    print "  `id` int(10) unsigned NOT NULL auto_increment,"
    print "  `channel_id` int(10) unsigned NOT NULL,"
    print "  `property` varchar(45) NOT NULL,"
    print "  `value` varchar(45) default NULL,"
    print "  `owner` varchar(45) NOT NULL,"
    print "  PRIMARY KEY  (`id`),"
    print "  KEY `property_channel` (`channel_id`),"
    print "  KEY `property_name` (`property`),"
    print "  CONSTRAINT `property_channel` FOREIGN KEY (`channel_id`) REFERENCES `channel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION"
    print ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"

def insert_channel(name, owner):
    global chan_id
    chan_id = chan_id + 1;
    print "INSERT INTO channel (id,name,owner) VALUE ("+`chan_id`+",'"+name+"','"+owner+"');"
    return chan_id

def insert_property_begin(channel, name, owner, value):
    global prop_id
    prop_id = prop_id + 1
    print "INSERT INTO property (id,channel_id,property,owner,value) VALUES ("+`prop_id`+","+`channel`+",'"+name+"','"+owner+"','"+value+"')",

def insert_property(channel, name, owner, value):
    global prop_id
    prop_id = prop_id + 1
    print ",("+`prop_id`+","+`channel`+",'"+name+"','"+owner+"','"+value+"')",

def insert_tag(channel, name, owner):
    global prop_id
    prop_id = prop_id + 1
    print ",("+`prop_id`+","+`channel`+",'"+name+"','"+owner+"',DEFAULT)",

def insert_property_end():
    print ";"

# for dummy channel information
def insert_bunch(count, prefix, midfix, postfix, location, cell, element, device, unit, sigtype):
    global chan_in_cell, buckets, val_bucket, full_buckets
    if (count > 9):
        cw = 2
    else:
        cw = 1
    for n in range(1,count+1):
        if (count == 1):
            cid = insert_channel(prefix+`cell`.zfill(3)+"-"+midfix+postfix, cowner)
        else:
            cid = insert_channel(prefix+`cell`.zfill(3)+"-"+midfix+`n`.zfill(cw)+postfix, cowner)
        chan_in_cell += 1

# Insert "real" properties
        insert_property_begin(cid,"location",powner,location)
        insert_property(cid,"cell",powner,`cell`.zfill(3))
        insert_property(cid,"element",powner,element)
        insert_property(cid,"device",powner,device)
        if (count != 1):
            insert_property(cid,"family",powner,`n`.zfill(cw))
        insert_property(cid,"unit",powner,unit)
        insert_property(cid,"type",powner,sigtype)

        pos_c = round((10.0/(count+1)) * n, 3)
        insert_property(cid,"z pos r",powner,'%.3f' % pos_c)
        insert_property(cid,"z pos a",powner,'%.3f' % (pos_c+(cell-1)*10.0))

        if (postfix.endswith("}T:1-RB")):
            insert_property(cid,"mount",powner,"outside")
        elif (postfix.endswith("}T:2-RB")):
            insert_property(cid,"mount",powner,"inside")
        elif (postfix.endswith("}T:3-RB")):
            insert_property(cid,"mount",powner,"top")
        elif (postfix.endswith("}T:4-RB")):
            insert_property(cid,"mount",powner,"bottom")
        else:
            insert_property(cid,"mount",powner,"center")

# group0 ... group5: Random distributions of 500/200/100/50/20/10/5/2/1 values and tags
        for g in range(0, 6):
            inx = random.randint(0, len(tokens[g])-1)
            val = tokens[g].pop(inx)
            insert_property(cid,"group"+`g`,powner,val)
            insert_tag(cid,"group"+`g`+"-"+val,towner)

# group6 ... group9: Ordered distributions of 500/200/100/50/20/10/5/2/1 values and tags
        if (chan_in_cell%2 == 1):
            insert_property(cid,"group6",powner,"500")
            insert_tag(cid,"group6-500",towner)
        elif (chan_in_cell <= 2*200):
            insert_property(cid,"group6",powner,"200")
            insert_tag(cid,"group6-200",towner)
        elif (chan_in_cell <= 2*(200+100)):
            insert_property(cid,"group6",powner,"100")
            insert_tag(cid,"group6-100",towner)
        elif (chan_in_cell <= 2*(200+100+50)):
            insert_property(cid,"group6",powner,"50")
            insert_tag(cid,"group6-50",towner)
        elif (chan_in_cell <= 2*(200+100+50+20)):
            insert_property(cid,"group6",powner,"20")
            insert_tag(cid,"group6-20",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10)):
            insert_property(cid,"group6",powner,"10")
            insert_tag(cid,"group6-10",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10+5)):
            insert_property(cid,"group6",powner,"5")
            insert_tag(cid,"group6-5",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10+5+2)):
            insert_property(cid,"group6",powner,"2")
            insert_tag(cid,"group6-2",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10+5+2+1)):
            insert_property(cid,"group6",powner,"1")
            insert_tag(cid,"group6-1",towner)
        else:
            insert_property(cid,"group6",powner,"0")
            insert_tag(cid,"group6-0",towner)

        if (chan_in_cell%2 == 0):
            insert_property(cid,"group7",powner,"500")
            insert_tag(cid,"group7-500",towner)
        elif (chan_in_cell <= 2*200):
            insert_property(cid,"group7",powner,"200")
            insert_tag(cid,"group7-200",towner)
        elif (chan_in_cell <= 2*(200+100)):
            insert_property(cid,"group7",powner,"100")
            insert_tag(cid,"group7-100",towner)
        elif (chan_in_cell <= 2*(200+100+50)):
            insert_property(cid,"group7",powner,"50")
            insert_tag(cid,"group7-50",towner)
        elif (chan_in_cell <= 2*(200+100+50+20)):
            insert_property(cid,"group7",powner,"20")
            insert_tag(cid,"group7-20",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10)):
            insert_property(cid,"group7",powner,"10")
            insert_tag(cid,"group7-10",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10+5)):
            insert_property(cid,"group7",powner,"5")
            insert_tag(cid,"group7-5",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10+5+2)):
            insert_property(cid,"group7",powner,"2")
            insert_tag(cid,"group7-2",towner)
        elif (chan_in_cell <= 2*(200+100+50+20+10+5+2+1)):
            insert_property(cid,"group7",powner,"1")
            insert_tag(cid,"group7-1",towner)
        else:
            insert_property(cid,"group7",powner,"0")
            insert_tag(cid,"group7-0",towner)

        if (chan_in_cell <= 500):
            insert_property(cid,"group8",powner,"500")
            insert_tag(cid,"group8-500",towner)
        elif (chan_in_cell <= 500+200):
            insert_property(cid,"group8",powner,"200")
            insert_tag(cid,"group8-200",towner)
        elif (chan_in_cell <= 500+(200+100)):
            insert_property(cid,"group8",powner,"100")
            insert_tag(cid,"group8-100",towner)
        elif (chan_in_cell <= 500+(200+100+50)):
            insert_property(cid,"group8",powner,"50")
            insert_tag(cid,"group8-50",towner)
        elif (chan_in_cell <= 500+(200+100+50+20)):
            insert_property(cid,"group8",powner,"20")
            insert_tag(cid,"group8-20",towner)
        elif (chan_in_cell <= 500+(200+100+50+20+10)):
            insert_property(cid,"group8",powner,"10")
            insert_tag(cid,"group8-10",towner)
        elif (chan_in_cell <= 500+(200+100+50+20+10+5)):
            insert_property(cid,"group8",powner,"5")
            insert_tag(cid,"group8-5",towner)
        elif (chan_in_cell <= 500+(200+100+50+20+10+5+2)):
            insert_property(cid,"group8",powner,"2")
            insert_tag(cid,"group8-2",towner)
        elif (chan_in_cell <= 500+(200+100+50+20+10+5+2+1)):
            insert_property(cid,"group8",powner,"1")
            insert_tag(cid,"group8-1",towner)
        else:
            insert_property(cid,"group8",powner,"0")
            insert_tag(cid,"group8-0",towner)

        if (chan_in_cell > 500):
            insert_property(cid,"group9",powner,"500")
            insert_tag(cid,"group9-500",towner)
        elif (chan_in_cell > 500-200):
            insert_property(cid,"group9",powner,"200")
            insert_tag(cid,"group9-200",towner)
        elif (chan_in_cell > 500-200-100):
            insert_property(cid,"group9",powner,"100")
            insert_tag(cid,"group9-100",towner)
        elif (chan_in_cell > 500-200-100-50):
            insert_property(cid,"group9",powner,"50")
            insert_tag(cid,"group9-50",towner)
        elif (chan_in_cell > 500-200-100-50-20):
            insert_property(cid,"group9",powner,"20")
            insert_tag(cid,"group9-20",towner)
        elif (chan_in_cell > 500-200-100-50-20-10):
            insert_property(cid,"group9",powner,"10")
            insert_tag(cid,"group9-10",towner)
        elif (chan_in_cell > 500-200-100-50-20-10-5):
            insert_property(cid,"group9",powner,"5")
            insert_tag(cid,"group9-5",towner)
        elif (chan_in_cell > 500-200-100-50-20-10-5-2):
            insert_property(cid,"group9",powner,"2")
            insert_tag(cid,"group9-2",towner)
        elif (chan_in_cell > 500-200-100-50-20-10-5-2-1):
            insert_property(cid,"group9",powner,"1")
            insert_tag(cid,"group9-1",towner)
        else:
            insert_property(cid,"group9",powner,"0")
            insert_tag(cid,"group9-0",towner)

        for p in range(20,max_prop):
            insert_property(cid,"prop"+`p`.zfill(2),powner,`chan_in_cell`+"-"+`p`.zfill(2))

        for p in range(11,max_tag):
            insert_tag(cid,"tag"+`p`.zfill(2),towner)

        if (cell%9 == 0):
            insert_tag(cid,"tagnine",towner)
        elif (cell%8 == 0):
            insert_tag(cid,"tageight",towner)
        elif (cell%7 == 0):
            insert_tag(cid,"tagseven",towner)
        elif (cell%6 == 0):
            insert_tag(cid,"tagsix",towner)
        elif (cell%5 == 0):
            insert_tag(cid,"tagfive",towner)
        elif (cell%4 == 0):
            insert_tag(cid,"tagfour",towner)
        elif (cell%3 == 0):
            insert_tag(cid,"tagthree",towner)
        elif (cell%2 == 0):
            insert_tag(cid,"tagtwo",towner)
        else:
            insert_tag(cid,"tagone",towner)

        insert_property_end()

def insert_big_magnets(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "PS:", "{"+dev+"}I-RB",    loc, cell, element, "power supply", "current",     "readback")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}I-SP",    loc, cell, element, "power supply", "current",     "setpoint")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}On-Sw",   loc, cell, element, "power supply", "power",       "switch")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Rst-Cmd", loc, cell, element, "power supply", "reset",       "command")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}On-St",   loc, cell, element, "power supply", "power",       "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Acc-St",  loc, cell, element, "power supply", "access",      "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}OK-St",   loc, cell, element, "power supply", "sum error",   "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}T-St",    loc, cell, element, "power supply", "temperature", "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}F-St",    loc, cell, element, "power supply", "water flow",  "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Gnd-St",  loc, cell, element, "power supply", "ground",      "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Ctl-St",  loc, cell, element, "power supply", "control",     "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Val-St",  loc, cell, element, "power supply", "value",       "status")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}Fld-RB",  loc, cell, element,  "magnet", "field",           "readback")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}Fld-SP",  loc, cell, element,  "magnet", "field",           "setpoint")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}T:1-RB",  loc, cell, element,  "magnet", "temperature",     "readback")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}T:2-RB",  loc, cell, element,  "magnet", "temperature",     "readback")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}F-RB",    loc, cell, element,  "magnet", "water flow",      "readback")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}F:in-St",  loc, cell, element, "magnet", "water flow in",   "status")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}F:out-St", loc, cell, element, "magnet", "water flow out",  "status")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}F:dif-St", loc, cell, element, "magnet", "water flow diff", "status")

def insert_air_magnets(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "PS:", "{"+dev+"}I-RB",    loc, cell, element, "power supply", "current",     "readback")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}I-SP",    loc, cell, element, "power supply", "current",     "setpoint")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}On-Sw",   loc, cell, element, "power supply", "power",       "switch")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Rst-Cmd", loc, cell, element, "power supply", "reset",       "command")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}On-St",   loc, cell, element, "power supply", "power",       "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}Acc-St",  loc, cell, element, "power supply", "access",      "status")
    insert_bunch(count, prefix, "PS:", "{"+dev+"}OK-St",   loc, cell, element, "power supply", "sum error",   "status")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}Fld-RB",  loc, cell, element, "magnet", "field", "readback")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}Fld-SP",  loc, cell, element, "magnet", "field", "setpoint")
    insert_bunch(count, prefix, "MG:", "{"+dev+"}T-RB",    loc, cell, element, "magnet", "temperature", "readback")

def insert_valves(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "VA:", "{"+dev+"}Opn-Sw", loc, cell, element, "valve", "position", "switch")
    insert_bunch(count, prefix, "VA:", "{"+dev+"}Opn-St", loc, cell, element, "valve", "position", "status")

def insert_gauges(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "VA:", "{"+dev+"}P-RB",  loc, cell, element, "gauge", "pressure", "readback")
    insert_bunch(count, prefix, "VA:", "{"+dev+"}OK-St", loc, cell, element, "gauge", "error",   "status")

def insert_pumps(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "VA:", "{"+dev+"}I-RB",   loc, cell, element, "pump", "current",  "readback")
    insert_bunch(count, prefix, "VA:", "{"+dev+"}P-RB",   loc, cell, element, "pump", "pressure", "readback")
    insert_bunch(count, prefix, "VA:", "{"+dev+"}On-Sw",  loc, cell, element, "pump", "power",    "switch")
    insert_bunch(count, prefix, "VA:", "{"+dev+"}OK-St",  loc, cell, element, "pump", "error",    "status")
    insert_bunch(count, prefix, "VA:", "{"+dev+"}On-St",  loc, cell, element, "pump", "power",    "status")

def insert_temps(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "PU:T", "{"+dev+"}T:1-RB", loc, cell, element, "sensor", "temperature 1", "readback")
    insert_bunch(count, prefix, "PU:T", "{"+dev+"}T:2-RB", loc, cell, element, "sensor", "temperature 2", "readback")
    insert_bunch(count, prefix, "PU:T", "{"+dev+"}T:3-RB", loc, cell, element, "sensor", "temperature 3", "readback")
    insert_bunch(count, prefix, "PU:T", "{"+dev+"}T:4-RB", loc, cell, element, "sensor", "temperature 4", "readback")
    insert_bunch(count, prefix, "PU:T", "{"+dev+"}On-St",  loc, cell, element, "sensor", "power", "status")

def insert_bpms(count, prefix, dev, loc, cell, element):
    insert_bunch(count, prefix, "BI:", "{"+dev+"}Pos:X-RB", loc, cell, element, "bpm", "x position", "readback")
    insert_bunch(count, prefix, "BI:", "{"+dev+"}Pos:Y-RB", loc, cell, element, "bpm", "y position", "readback")
    insert_bunch(count, prefix, "BI:", "{"+dev+"}Sig:X-RB", loc, cell, element, "bpm", "x sigma", "readback")
    insert_bunch(count, prefix, "BI:", "{"+dev+"}Sig:Y-RB", loc, cell, element, "bpm", "y sigma", "readback")
    insert_bunch(count, prefix, "BI:", "{"+dev+"}On-St",    loc, cell, element, "bpm", "power", "status")

def insert_sr_cell(cell):
    global chan_in_cell, tokens, val_bucket
    loc = "storage ring"
    pre = "SR:C"
    chan_in_cell = 0
    my_buck = [112, 1, 2, 5, 10, 20, 50, 100, 200, 500]
    tokens = []
    for g in range(0, 6):
        tok = []
        for x in range (0, 10):
            for y in range (0, my_buck[x]):
                tok.append(val_bucket[x])
        tokens.append(tok)

    insert_big_magnets(2, pre, "DP",   loc, cell, "dipole")
    insert_big_magnets(5, pre, "QDP:D",  loc, cell, "defocusing quadrupole")
    insert_big_magnets(5, pre, "QDP:F",  loc, cell, "focusing quadrupole")
    insert_big_magnets(4, pre, "QDP:S",  loc, cell, "skew quadrupole")
    insert_big_magnets(4, pre, "STP",   loc, cell, "sextupole")
    insert_big_magnets(5, pre, "HC:S", loc, cell, "horizontal slow corrector")
    insert_air_magnets(5, pre, "HC:F", loc, cell, "horizontal fast corrector")
    insert_big_magnets(5, pre, "VC:S", loc, cell, "vertical slow corrector")
    insert_air_magnets(4, pre, "VC:F", loc, cell, "vertical fast corrector")

    insert_valves(5, pre, "GV", loc, cell, "vacuum")
    insert_gauges(5, pre, "VGC", loc, cell, "vacuum")
    insert_gauges(5, pre, "TCG", loc, cell, "vacuum")
    insert_pumps(2, pre, "IPC", loc, cell, "vacuum")
    insert_pumps(2, pre, "TMP", loc, cell, "vacuum")

    insert_temps(40, pre, "TC", loc, cell, "temperature sensor")

    insert_bpms(4, pre, "BSA", loc, cell, "small aperture BPM")
    insert_bpms(4, pre, "BHS", loc, cell, "high stability BPM")
    insert_bpms(4, pre, "BLA", loc, cell, "large aperture BPM")

def insert_bo_cell(cell):
    global chan_in_cell, tokens, val_bucket
    loc = "booster"
    pre = "BR:C"
    chan_in_cell = 0
    my_buck = [112, 1, 2, 5, 10, 20, 50, 100, 200]
    tokens = []
    for g in range(0, 6):
        tok = []
        for x in range (0, 9):
            for y in range (0, my_buck[x]):
                tok.append(val_bucket[x])
        tokens.append(tok)

    insert_big_magnets(2, pre, "DP",  loc, cell, "dipole")
    insert_big_magnets(4, pre, "QDP:D", loc, cell, "defocusing quadrupole")
    insert_big_magnets(4, pre, "QDP:F", loc, cell, "focusing quadrupole")
    insert_big_magnets(2, pre, "STP",  loc, cell, "sextupole")
    insert_big_magnets(4, pre, "HC", loc, cell, "horizontal corrector")
    insert_big_magnets(4, pre, "VC", loc, cell, "vertical corrector")

    insert_valves(4, pre, "GV", loc, cell, "vacuum")
    insert_gauges(4, pre, "VGC", loc, cell, "vacuum")
    insert_gauges(2, pre, "TCG", loc, cell, "vacuum")
    insert_pumps(2, pre, "IPC", loc, cell, "vacuum")
    insert_pumps(2, pre, "TMP", loc, cell, "vacuum")

    insert_temps(10, pre, "TC", loc, cell, "temperature sensor")

    insert_bpms(2, pre, "BLA", loc, cell, "beam position monitor")

def main():
    """main procedure
    """

    create_db()

    # create dummy channel
    # 1000 channels per sr cell
    for n in range (1,101):
        cell = `n`.zfill(3)
        insert_sr_cell(n)

    # 500 channels per bo cell
    for n in range (1,101):
        cell = `n`.zfill(3)
        insert_bo_cell(n)

if __name__ == "__main__":
        main()
