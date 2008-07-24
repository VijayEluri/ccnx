/*
 * Dumps everything quickly retrievable to stdout
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ccn/ccn.h>

/***********
<Interest>
  <Name/>
  <OrderPreference>4</OrderPreference>
  <Scope>0</Scope>
</Interest>
**********/
static const unsigned char templ_ccnb[15] =
        "\001\322\362\000\002\362\216\064"
        "\000\002\322\216\060\000\000";

int
incoming_content(
    struct ccn_closure *selfp,
    enum ccn_upcall_kind kind,
    struct ccn *h,
    const unsigned char *ccnb,    /* binary-format Interest or ContentObject */
    size_t ccnb_size,             /* size in bytes */
    struct ccn_indexbuf *comps,   /* component boundaries within ccnb */
    int matched_comps,            /* number of components in registration */
    const unsigned char *matched_ccnb, /* binary-format matched Interest */
    size_t matched_ccnb_size
)
{
    struct ccn_charbuf *c = ccn_charbuf_create();
    struct ccn_charbuf *templ = ccn_charbuf_create();
    if (kind == CCN_UPCALL_FINAL)
        return(0);
    if (kind == CCN_UPCALL_INTEREST_TIMED_OUT)
        return(CCN_UPCALL_RESULT_REEXPRESS);
    if (kind != CCN_UPCALL_CONTENT)
        return(-1);
    fwrite(ccnb, ccnb_size, 1, stdout);
    /* Use the name of the content just received as the resumption point */
    ccn_name_init(c);
    c->length--;
    ccn_charbuf_append(c, ccnb + comps->buf[0], comps->buf[comps->n-1] - comps->buf[0]);
    ccn_charbuf_append_closer(c);
    ccn_charbuf_append(templ, templ_ccnb, 15);
    ccn_express_interest(h, c, 0, selfp, templ);
    ccn_charbuf_destroy(&templ);
    ccn_charbuf_destroy(&c);
    selfp->data = selfp; /* make not NULL to indicate we got something */
    return(0);
}

/* Use some static data for this simple program */
static struct ccn_closure incoming_content_action = {
    .p = &incoming_content
};

int
main(int argc, char **argv)
{
    struct ccn *ccn = NULL;
    struct ccn_charbuf *c = NULL;
    struct ccn_charbuf *templ = NULL;
    int i;
    ccn = ccn_create();
    if (ccn_connect(ccn, NULL) == -1) {
        perror("Could not connect to ccnd");
        exit(1);
    }
    c = ccn_charbuf_create();
    templ = ccn_charbuf_create();
    /* set scope to only address ccnd */
    ccn_charbuf_append(templ, templ_ccnb, 15);
    ccn_name_init(c);
    ccn_express_interest(ccn, c, 0, &incoming_content_action, templ);
    for (i = 0; i < 1000; i++) {
        incoming_content_action.data = NULL;
        ccn_run(ccn, 100); /* stop if we run dry for 1/10 sec */
        fflush(stdout);
        if (incoming_content_action.data == NULL)
            break;
    }
    ccn_destroy(&ccn);
    exit(0);
}
