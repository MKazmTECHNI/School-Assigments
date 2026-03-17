# Project Purpose

Program takes in file `names.csv`, takes each name, and fixes typos, unessesary whitespaces, particles or punctuation and returns `result.csv` with closest known name and surname. I've attached example files.

## Levenshtein's String Comparing

I use Levenshtein's Algorythm, to calculate distance between input and each string in huge list of names, to return the one that's closest to the original string.

## Usage:

Just put the names you need into names.csv. The number next to names was required for a project, and isn't important, you can cut it out yourself pretty quickly and easily.

### IMPORTANT - `names.py` and `surnames.py` DO NOT have all names in the world.

There might be some names that have been inserted correctly, but came out as completly diffrent ones. I tried my best to add as many names and surnames and insert it into files, but I've already found some examples that should've been there and wasn't. **If you find bigger/better list or bunch of names that aren't in, feel free to send them, and I'll update it**

### "why is it so... unfinished? (ugly)"

Cuz i wrote most of it on power of a hunch, depression, faith its gonna work, cafeine and long ago. If you're in need for faster version, look for implementation in C, I've tried my best alr?

## May be added

- In when comparing, if levenshtein distance is greated than `'min_dist'`, stop comparing with this name. Should make it slightly faster.

- If you insert "Anna Kowalski" it'll go through even tho its masculine surname. if you're that sensitive about that, be my guest, go over 7000 names and surnames, and add 1 if its a dude and 0 if its a girl, then modify code slightly to change the end, but I'm aint doing allat (it shouldn't really have an impact later either way)

Other than lack of my belowed one liners, I'm convinced it works well and fairly fast.
